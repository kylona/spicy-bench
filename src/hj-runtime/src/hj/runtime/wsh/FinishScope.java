package hj.runtime.wsh;

import edu.rice.hj.api.HjPhaserPair;
import hj.runtime.phasers.Phaser;
import java.util.Iterator;
import java.util.LinkedList;
import hj.util.SyncLock;

/**
 *
 * @author bchase
 * @author Peter Anderson <anderson.peter@byu.edu>
 */
public class FinishScope {

    // This is not apart of original library
    // Used to track information within a single Finish block
    // Information includes all Activities and Phasers within it.
    // Also makes code cleaner in Activity
    private final LinkedList<Activity> scope;
    private final SyncLock lock;
    private final LinkedList<HjPhaserPair> phaserPairs;

    public FinishScope() {
        scope = new LinkedList<>();
        lock = new SyncLock();
        phaserPairs = new LinkedList<>();
    }

    public void add(Activity activity) {
        lock.lock();
        try {
            scope.add(activity);
        } finally {
            lock.unlock();
        }
    }

    public Activity poll() {
        Activity result = null;
        lock.lock();
        try {
            result = scope.poll();
        } finally {
            lock.unlock();
        }
        return result;
    }

    public int size() {
        int result;
        lock.lock();
        try {
            result = scope.size();
        } finally {
            lock.unlock();
        }
        return result;
    }

    public void addPhaser(HjPhaserPair ph) {
        lock.lock();
        try {
            phaserPairs.add(ph);
        } finally {
            lock.unlock();
        }
    }

    public void unregisterAllPhasers(Activity owner) {
        lock.lock();
        try {
            Iterator<HjPhaserPair> iter = phaserPairs.iterator();
            while (iter.hasNext()) {
                HjPhaserPair ph = iter.next();
                Phaser t_phaser = (Phaser) ph.phaser;
                t_phaser.unregisterSignaler(owner);
                t_phaser.unregisterWaiter(owner);
                owner.removePhaser(ph);
            }
        } finally {
            lock.unlock();
        }
    }
}
