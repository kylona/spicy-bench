package hj.util;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.TimeUnit;

/**
 * Naive lock implementation using Java monitors. Focused on JPF compatibility
 * rather than performance. This lock is reentrant.
 */
public class SyncLock implements Lock {

    private final Object monitor = new Object();
    private Object owner;
    private int depth;

    private boolean tryTakeOwnership() {
        Object current = Thread.currentThread();
        if (owner == null) {
            owner = current;
            depth = 1;
        } else if (owner == current) {
            depth++;
        } else {
            return false;
        }
        return true;
    }

    private void confirmOwnership() {
        if (owner != Thread.currentThread()) {
            throw new IllegalMonitorStateException("must own lock");
        }
    }

    private void releaseOwnership() {
        confirmOwnership();
        if (--depth == 0) {
            owner = null;
            monitor.notifyAll();
        }
    }

    private int releaseAllOwnership() {
        confirmOwnership();
        int savedDepth = depth;
        owner = null;
        depth = 0;
        monitor.notifyAll();
        return savedDepth;
    }

    private void restoreOwnership(int savedDepth) {
        lock();
        depth = savedDepth;
    }

    private void lockBase(boolean retry, long duration, TimeUnit unit, boolean interruptible) {
        synchronized (monitor) {
            while (!tryTakeOwnership() & retry) {
                checkedWait(monitor, duration, unit, interruptible);
            }
        }
    }

    public boolean isHeldByCurrentThread() {
        boolean res;
        synchronized (monitor) {
            res = (owner == Thread.currentThread());
        }
        return res;
    }

    @Override
    public void lock() {
        lockBase(true, 0, null, false);
    }

    @Override
    public boolean tryLock() {
        lockBase(false, 0, null, false);
        return isHeldByCurrentThread();
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        try {
            lockBase(false, time, unit, true);
        } catch (InterruptRuntimeException e) {
            throw e.getCause();
        }
        return isHeldByCurrentThread();
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        try {
            lockBase(true, 0, null, true);
        } catch (InterruptRuntimeException e) {
            throw e.getCause();
        }
    }

    @Override
    public void unlock() {
        synchronized (monitor) {
            releaseOwnership();
        }
    }

    @Override
    public Condition newCondition() {
        return new SyncCondition();
    }

    private class SyncCondition implements Condition {

        private final Object conditionMonitor = new Object();

        private void awaitBase(boolean retry, long t, TimeUnit unit, boolean interruptible) {
            confirmOwnership();
            int savedState;
            synchronized (conditionMonitor) {
                synchronized (monitor) {
                    savedState = releaseAllOwnership();
                }
                checkedWait(conditionMonitor, t, unit, interruptible);
            }
            synchronized (monitor) {
                restoreOwnership(savedState);
            }
        }

        @Override
        public void await() throws InterruptedException {
            try {
                awaitBase(true, 0, null, true);
            } catch (InterruptRuntimeException e) {
                throw e.getCause();
            }
        }

        @Override
        public void awaitUninterruptibly() {
            awaitBase(true, 0, null, false);
        }

        @Override
        public boolean await(long time, TimeUnit unit) throws InterruptedException {
            return awaitNanos(unit.toNanos(time)) > 0;
        }

        @Override
        public long awaitNanos(long time) throws InterruptedException {
            long start = System.nanoTime();
            try {
                awaitBase(true, time, TimeUnit.NANOSECONDS, true);
            } catch (InterruptRuntimeException e) {
                throw e.getCause();
            }
            return System.nanoTime() - start;
        }

        @Override
        public boolean awaitUntil(java.util.Date deadline) throws InterruptedException {
            long t = deadline.getTime() - System.currentTimeMillis();
            return await(t, TimeUnit.MILLISECONDS);
        }

        @Override
        public void signal() {
            confirmOwnership();
            synchronized (conditionMonitor) {
                conditionMonitor.notify();
            }
        }

        @Override
        public void signalAll() {
            confirmOwnership();
            synchronized (conditionMonitor) {
                conditionMonitor.notifyAll();
            }
        }
    }

    private static void checkedWait(Object target, long duration, TimeUnit unit, boolean interruptible) {
        while (true) {
            try {
                if (unit != null) {
                    unit.timedWait(target, duration);
                } else {
                    target.wait();
                }
                return;
            } catch (InterruptedException e) {
                if (interruptible) {
                    throw new InterruptRuntimeException(e);
                }
            }
        }
    }

    private static class InterruptRuntimeException extends RuntimeException {

        InterruptedException cause;

        InterruptRuntimeException(InterruptedException cause) {
            super(cause);
            this.cause = cause;
        }

        @Override
        public InterruptedException getCause() {
            return cause;
        }
    }
}
