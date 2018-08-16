package hj.lang;

import edu.rice.hj.api.HjFuture;
import edu.rice.hj.api.HjSuspendingCallable;
import edu.rice.hj.api.SuspendableException;
import hj.runtime.wsh.Activity;

import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Peter Anderson <anderson.peter@byu.edu>
 * @param <V>
 */
@SuppressWarnings("rawtypes")
public class Future<V> extends Activity implements HjFuture<V> {

    private V contents;
    private final CountDownLatch lock;
    private boolean resolved;
    private boolean failed;
    private boolean started;
    private final HjSuspendingCallable callable;

    public Future(HjSuspendingCallable callable) {
        super(false, null, null);
        this.callable = callable;
        lock = new CountDownLatch(1);
        failed = false;
        started = false;
        resolved = false;
    }

    @Override
    public void run() {
        if (!failed) {
            started = true;
            try {
                contents = (V) callable.call();
            } catch (SuspendableException ex) {
                Logger.getLogger(Future.class.getName()).log(Level.SEVERE, null, ex);
                ex.printStackTrace();
            }
            lock.countDown();
            resolved = true;
        }
    }

    // The object is a final so the reference will not change but the object contents may change
    // If the contents change there may be a data race, which we want to catch
    @Override
    public V get() {
        if (failed) {
            return null;
        }

        try {
            lock.await();
        } catch (InterruptedException ex) {
            System.err.println("Future failed");
            System.exit(1);
        }
        return contents;
    }

    @Override
    public boolean cancel() {
        if (!started) {
            failed = true;
            return failed;
        }
        return false;
    }

    @Override
    public boolean failed() {
        return failed;
    }

    @Override
    public boolean resolved() {
        return resolved;
    }

    //NOTE: the safeget is only allowed to be called on a resolved future.
    @Override
    public V safeGet() {
        if (resolved) {
            return contents;
        }
        throw new IllegalStateException("Safe get not allowed on unresolved future!");
    }

}
