package hj.runtime.wsh;

import edu.rice.hj.api.HjSuspendable;
import edu.rice.hj.api.SuspendableException;

/**
 *
 * @author Peter Anderson <anderson.peter@byu.edu>
 */
public class SuspendableActivity extends Activity {

    private final HjSuspendable suspendable;

    public SuspendableActivity(HjSuspendable suspendable, boolean isMain) {
        super(isMain, null, null);
        this.suspendable = suspendable;
    }

    @Override
    public void run() {
        try {
            try {
                suspendable.run();
            } catch (SuspendableException ex) {
                System.err.println("Caught Suspendable Exception");
            }
        } finally {
            assert (lockReleased()) : "Unreleased Lock";
            stopFinish();
            super.unregisterFromPhasers();
        }
    }
}
