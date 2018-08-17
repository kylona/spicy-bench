package hj.lang;

import edu.rice.hj.api.HjDataDrivenFuture;
import hj.util.SyncLock;

import java.util.concurrent.CountDownLatch;

public class DataDrivenFuture<V> implements HjDataDrivenFuture<V> {

    private V contents;
    private boolean valueAssignedOnce;
    private final CountDownLatch lock;
    private boolean failed;
    private final SyncLock interiorLock;

    public DataDrivenFuture() {
        valueAssignedOnce = false;
        lock = new CountDownLatch(1);
        failed = false;
        interiorLock = new SyncLock();
    }

    @Override
    public void put(V in) {
        try {
            interiorLock.lock();
            //Invalid put conditions.
            if (valueAssignedOnce) {
                throw new IllegalStateException("attempted duplicate set on DDC!");
            }
            if (failed) {
                throw new CancelledException("Cannot set data on cancled DDF!");
            }
            if (in == null) {
                throw new CancelledException("Cannot set null data on DDF!");
            }

            //Conditions valid for put.
            valueAssignedOnce = true;
            contents = in;
            lock.countDown();

        } finally {
            interiorLock.unlock();
        }
    }

    @Override
    public V get() {
        try {
            interiorLock.lock();
            if (!valueAssignedOnce) {
                throw new IllegalStateException("No value available in the DDF!");
            }
            if (failed) {
                throw new CancelledException("Cannot retrieve value from canceled DDF!");
            }
            return contents;

        } finally {
            interiorLock.unlock();
        }
    }

    @Override
    public boolean cancel() {
        try {
            interiorLock.lock();
            if (!valueAssignedOnce) {
                failed = true;
                return true;
            }
            return false;
        } finally {
            interiorLock.unlock();
        }
    }

    @Override
    public boolean failed() {
        try {
            interiorLock.lock();
            return failed;
        } finally {
            interiorLock.unlock();
        }
    }

    @Override
    public boolean resolved() {
        try {
            interiorLock.lock();
            return valueAssignedOnce;
        } finally {
            interiorLock.unlock();
        }
    }

    @Override
    public V safeGet() {
        try {
            interiorLock.lock();
            if (valueAssignedOnce) {
                return contents;
            }
            throw new IllegalStateException("Safe get not allowed on unresolved future!");
        } finally {
            interiorLock.unlock();
        }
    }

    @Override
    public void failed(Exception exceptions) {
        //There is no way to retrieve this information in the default HJ interface. Storing it is pointless.
    }
}
