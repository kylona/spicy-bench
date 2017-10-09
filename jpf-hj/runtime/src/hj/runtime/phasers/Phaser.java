package hj.runtime.phasers;

import edu.rice.hj.api.HjPhaserMode;
import edu.rice.hj.api.HjPhaser;
import edu.rice.hj.api.HjPhaserPair;
import hj.runtime.wsh.Activity;
import hj.runtime.wsh.FinishScope;
import java.util.HashMap;
import java.util.concurrent.locks.Condition;
import hj.util.SyncLock;

/**
 * @author bchase
 * @author Peter Anderson <anderson.peter@byu.edu>
 * @author Kris
 */
public class Phaser implements HjPhaser {

    private int yetToSignal;
    private final HashMap<Integer, Integer> signalers;
    private final HashMap<Integer, Integer> waiters;
    private final Condition phaseAdvance;
    private final SyncLock lock;
    private final FinishScope scope;
    private HjPhaserMode mode;
    private int phaseNumber;

    public Phaser(HjPhaserMode mode) {
        this.mode = mode;
        lock = new SyncLock();
        yetToSignal = 0;
        signalers = new HashMap<>();
        waiters = new HashMap<>();
        phaseAdvance = lock.newCondition();
        phaseNumber = 0;

        Activity parent = (Activity) Thread.currentThread();
        scope = parent.getCurrentFinishScope();

        if (mode != HjPhaserMode.WAIT) {
            registerSignaler(parent);
        }
        if (mode != HjPhaserMode.SIG) {
            registerWaiter(parent);
        }

        HjPhaserPair pair = new HjPhaserPair(this, mode);
        scope.addPhaser(pair);
        parent.addPhaser(pair);
    }

    public Phaser() {
        this(HjPhaserMode.SIG_WAIT);
    }

    public final void registerSignaler(Activity newParty) {
        try {
            lock.lock();
            if (newParty == null) {
                return;
            }
            if (signalers.containsKey(newParty.ID) == false) {
                signalers.put(newParty.ID, 0);
                yetToSignal++;
            }
        } finally {
            lock.unlock();
            assert (!lock.isHeldByCurrentThread()) : "Failed to release phaser lock";
        }
    }

    public final void registerWaiter(Activity newParty) {
        try {
            lock.lock();
            if (newParty == null) {
                return;
            }
            if (waiters.containsKey(newParty.ID) == false) {
                waiters.put(newParty.ID, 0);
            }
        } finally {
            lock.unlock();
            assert (!lock.isHeldByCurrentThread()) : "Failed to release phaser lock";
        }
    }

    public void unregisterSignaler(Activity existingParty) {
        try {
            lock.lock();
            if (existingParty == null) {
                return;
            }
            if (signalers.containsKey(existingParty.ID)) {
                int value = signalers.get(existingParty.ID);
                if (value == 0) {
                    yetToSignal--;
                }
                signalers.remove(existingParty.ID);
                if (checkAdvancePhase()) {
                    advancePhase();
                }
            }
        } finally {
            lock.unlock();
            assert (!lock.isHeldByCurrentThread()) : "Failed to release phaser lock";
        }
    }

    public void unregisterWaiter(Activity existingParty) {
        try {
            lock.lock();
            if (existingParty == null) {
                return;
            }
            if (waiters.containsKey(existingParty.ID)) {
                waiters.remove(existingParty.ID);
            }
        } finally {
            lock.unlock();
            assert (!lock.isHeldByCurrentThread()) : "Failed to release phaser lock";
        }
    }

    private void advancePhase() {
        try {
            lock.lock();
            phaseNumber++;
            phaseAdvance.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public void phSignal(Activity signaler) {
        try {
            lock.lock();
            if (signalers.containsKey(signaler.ID)) {
                int currentSignalerID = signalers.get(signaler.ID);
                signalers.put(signaler.ID, currentSignalerID + 1);
                if (currentSignalerID == phaseNumber) {
                    yetToSignal--;
                }
                if (checkAdvancePhase()) {
                    advancePhase();
                    resetUnsignaledCount();
                }
            }
        } finally {
            lock.unlock();
            assert (!lock.isHeldByCurrentThread()) : "Failed to release phaser lock";
        }
    }

    public void phWait(Activity waiter) {
        try {
            lock.lock();
            if (waiter == null) {
                return;
            }
            if (waiters.containsKey(waiter.ID) && waiters.keySet() != null) {
                int waitValue = waiters.get(waiter.ID);
                while (waitValue >= phaseNumber) {
                    phaseAdvance.await();
                }
                waiters.put(waiter.ID, waitValue + 1);
            }

        } catch (InterruptedException ex) {
            System.out.println("Waiting Thread was interrupted");
        } finally {
            lock.unlock();
            assert (!lock.isHeldByCurrentThread()) : "Failed to release phaser lock";
        }
    }

    private boolean checkAdvancePhase() {
        if (yetToSignal != 0) {
            return false;
        }

        return true;
    }

    @Override
    public HjPhaserMode getPhaserMode() {
        Activity activity = (Activity) Thread.currentThread();
        try {
            return activity.getPhaserMode(this);
        } catch (Exception e) {
            System.exit(1);
            return null;
        }
    }

    @Override
    public void doNext() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void doWait() {
        Activity activity = (Activity) Thread.currentThread();
        phWait(activity);
    }

    @Override
    public void drop() {
        Activity activity = (Activity) Thread.currentThread();
        unregisterSignaler(activity);
        unregisterWaiter(activity);
    }

    @Override
    public int getSigPhase() {
        return phaseNumber;
    }

    @Override
    public int getWaitPhase() {
        return phaseNumber;
    }

    @Override
    public HjPhaserPair inMode(HjPhaserMode phaserMode) {
        return new HjPhaserPair(this, phaserMode);
    }

    @Override
    public void signal() {
        Activity activity = (Activity) Thread.currentThread();
        phSignal(activity);
    }

    private void resetUnsignaledCount() {
        yetToSignal = signalers.keySet().size();
    }

}
