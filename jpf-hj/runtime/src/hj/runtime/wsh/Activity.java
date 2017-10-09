package hj.runtime.wsh;

import edu.rice.hj.api.HjPhaser;
import edu.rice.hj.api.HjPhaserMode;
import edu.rice.hj.api.HjPhaserPair;
import edu.rice.hj.api.HjRunnable;
import hj.runtime.phasers.Phaser;
import hj.lang.Runtime;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Activity extends Thread {

    // Descendants contains all Finish scopes the activity has
    private final Deque<FinishScope> descendants;
    // InheritedFinishScope is the finish scope the activity is in
    private final FinishScope scopeBelongsTo;
    // HJRunnable is the code for the task to begin executing
    private final HjRunnable runnable;
    public final int ID;
    private final LinkedList<HjPhaserPair> phasers;

    public Activity(boolean isMain, HjRunnable runnable,
            List<HjPhaserPair> phasers) {
        descendants = new ArrayDeque<>();
        this.phasers = new LinkedList<>();
        ID = Runtime.getUniqueActivityID();
        this.runnable = runnable;
        if (isMain) {
            // First activity (main) must set up a brand new finish scope
            descendants.push(new FinishScope());
            scopeBelongsTo = null;
        } else {
            // Add self to parent's most recent finish scope or parent's own scope
            Activity parentThread = (Activity) Thread.currentThread();
            scopeBelongsTo = parentThread.getCurrentFinishScope();
        }
        assignScope();
        assignPhasers(phasers);
    }

    final protected void assignScope() {
        if (scopeBelongsTo != null) {
            scopeBelongsTo.add(this);
        }
    }

    final protected void assignPhasers(List<HjPhaserPair> phasers) {
        if (phasers != null) {
            Iterator<HjPhaserPair> it = phasers.iterator();
            while (it.hasNext()) {
                HjPhaserPair phaserPair = it.next();
                this.phasers.add(phaserPair);
                Phaser t_phaser = (Phaser) phaserPair.phaser;
                if (phaserPair.mode != HjPhaserMode.WAIT) {
                    t_phaser.registerSignaler(this);
                }
                if (phaserPair.mode != HjPhaserMode.SIG) {
                    t_phaser.registerWaiter(this);
                }
            }
        }
    }

    @Override
    public void run() {
        try {
            runnable.run();
        } finally {
            assert (lockReleased()) : "Unreleased Lock";
            stopFinish();
            unregisterFromPhasers();
        }
    }

    // Get the FinishScope a newly created task would belong to
    public FinishScope getCurrentFinishScope() {
        if (descendants.size() == 0) {
            return scopeBelongsTo;
        } else {
            return descendants.peek();
        }
    }

    public void startFinish() {
        descendants.push(new FinishScope());
    }

    public void stopFinish() {
        FinishScope children = descendants.peek();
        if (children != null) {
            children.unregisterAllPhasers(this);
            while (children.size() > 0) {
                Activity child = (Activity) children.poll();
                try {
                    child.join();
                } catch (InterruptedException e) {
                }
            }
            descendants.pop();
        }
    }

    protected boolean lockReleased() {
        return Runtime.lockReleased();
    }

    // Non-blocking signal to all phasers
    public void doSignal() {
        Iterator<HjPhaserPair> iterator = phasers.iterator();
        while (iterator.hasNext()) {
            HjPhaserPair phaserPair = iterator.next();
            if (phaserPair.mode != HjPhaserMode.WAIT) {
                phaserPair.phaser.signal();
            }
        }
    }

    // Potentially blocking wait on all phasers
    public void doWait() {
        Iterator<HjPhaserPair> iterator = phasers.iterator();
        while (iterator.hasNext()) {
            HjPhaserPair phaserPair = iterator.next();
            if (phaserPair.mode != HjPhaserMode.SIG) {
                phaserPair.phaser.doWait();
            }
        }
    }

    public void doNext() {
        //First signal all phasers
        doSignal();
        //Next wait on all phasers
        doWait();
    }

    public HjPhaserMode getPhaserMode(HjPhaser phaser) throws Exception {
        for (HjPhaserPair ph : phasers) {
            if (phaser == ph.phaser) {
                return ph.mode;
            }
        }
        throw new Exception();
    }

    protected void unregisterFromPhasers() {
        for (HjPhaserPair phaserPair : phasers) {
            Phaser t_phaser = (Phaser) phaserPair.phaser;
            if (phaserPair.mode != HjPhaserMode.WAIT) {
                t_phaser.unregisterSignaler(this);
            }
            if (phaserPair.mode != HjPhaserMode.SIG) {
                t_phaser.unregisterWaiter(this);
            }
        }
    }

    public void addPhaser(HjPhaserPair pair) {
        phasers.add(pair);
    }

    public void removePhaser(HjPhaserPair pair) {
        phasers.remove(pair);
    }
}
