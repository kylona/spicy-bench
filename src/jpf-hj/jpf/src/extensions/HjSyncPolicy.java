/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package extensions;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.vm.AllRunnablesSyncPolicy;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.choice.ThreadChoiceFromSet;
import java.util.Random;

/**
 *
 * @author Peter Anderson <anderson.peter@byu.edu>
 */
public class HjSyncPolicy extends AllRunnablesSyncPolicy {

    public HjSyncPolicy(Config config) {
        super(config);
    }

    private static final Random randNumGen = new Random(0);

    protected ChoiceGenerator<ThreadInfo> getSingleChoiceCG(String id, ThreadInfo tiCurrent) {
        //In cases where there are multiple runnables a random choice is made
        ThreadInfo[] timeoutRunnables
                = getTimeoutRunnables(tiCurrent.getApplicationContext());
        if (timeoutRunnables.length == 0) {
            return null;
        } else if (timeoutRunnables.length == 1) {
            return new ThreadChoiceFromSet(id, timeoutRunnables, true);
        } else {
            //System.out.println("Many Choices");
            if (tiCurrent.isRunnable()) {
                ThreadInfo[] choice = {tiCurrent};
                return new ThreadChoiceFromSet(id, choice, true);
            } else {
                int nextChoiceIndex = randNumGen.nextInt(timeoutRunnables.length);
                ThreadInfo[] choice = {timeoutRunnables[nextChoiceIndex]};
                return new ThreadChoiceFromSet(id, choice, true);
            }
        }
    }

    protected boolean setSingleNonBlockingCG(String id, ThreadInfo tiCurrent) {
        if (!tiCurrent.isFirstStepInsn() || tiCurrent.isEmptyTransitionEnabled()) {
            if (vm.getSystemState().isAtomic()) {
                return false;
            } else {
                return setNextChoiceGenerator(getSingleChoiceCG(id, tiCurrent));
            }
        } else {
            return false;
        }
    }

    protected boolean setSingleBlockingCG(String id, ThreadInfo tiCurrent) {
        if (!tiCurrent.isFirstStepInsn() || tiCurrent.isEmptyTransitionEnabled()) {
            if (vm.getSystemState().isAtomic()) {
                vm.getSystemState().setBlockedInAtomicSection();
            }
            ChoiceGenerator<ThreadInfo> cg = getSingleChoiceCG(id, tiCurrent);
            if (cg == null) {
                if (vm.getThreadList().hasLiveThreads()) {
                    cg = blockedWithoutChoice;
                }
            }
            return setNextChoiceGenerator(cg);
        } else {
            return false;
        }
    }

    @Override
    protected boolean setNextChoiceGenerator(ChoiceGenerator<ThreadInfo> cg) {
        if (cg != null) {
            return vm.getSystemState().setNextChoiceGenerator(cg);
        } else {
            return false;
        }
    }

    protected boolean setSingleMaybeBlockingCG(String id, ThreadInfo tiCurrent,
            ThreadInfo tiBlock) {
        if (tiCurrent == tiBlock) {
            return setSingleBlockingCG(id, tiCurrent);
        } else {
            return setSingleNonBlockingCG(id, tiCurrent);
        }
    }

    //Zero Choice---------------------------------------------------------------
    //CGs are not inserted
    @Override
    public boolean setsStartCG(ThreadInfo ti, ThreadInfo ti1) {
        return false;
    }

    @Override
    public boolean setsLockReleaseCG(ThreadInfo ti, ElementInfo ei, boolean didUnblock) {
        return false;
    }

    // ??? -- How can I be sure we don't need to insert after notify
    @Override
    public boolean setsNotifyCG(ThreadInfo ti, boolean didNotify) {
        return false;
    }

    // ???
    @Override
    public boolean setsNotifyAllCG(ThreadInfo ti, boolean didNotify) {
        return false;
    }

    @Override
    public boolean setsPriorityCG(ThreadInfo ti) {
        return false;
    }

    @Override
    public boolean setsSleepCG(ThreadInfo ti, long millis, int nanos) {
        return false;
    }

    @Override
    public boolean setsSuspendCG(ThreadInfo tiCurrent, ThreadInfo tiSuspended) {
        return false;
    }

    @Override
    public boolean setsResumeCG(ThreadInfo tiCurrent, ThreadInfo tiResumed) {
        return false;
    }

    @Override
    public boolean setsStopCG(ThreadInfo tiCurrent, ThreadInfo tiStopped) {
        return false;
    }

    @Override
    public boolean setsInterruptCG(ThreadInfo tiCurrent, ThreadInfo tiInterrupted) {
        return false;
    }

    @Override
    public boolean setsYieldCG(ThreadInfo ti) {
        return false;
    }

    @Override
    public boolean setsUnparkCG(ThreadInfo tiCurrent, ThreadInfo tiUnparked) {
        return false;
    }

    @Override
    public boolean setsEndAtomicCG(ThreadInfo ti) {
        return false;
    }

    @Override
    public boolean setsLockAcquisitionCG(ThreadInfo ti, ElementInfo ei) {
        return false;
    }
    //--------------------------------------------------------------------------

    //Single Choice-------------------------------------------------------------
    //CGs are inserted because they are mandatory for proper system function
    @Override
    public boolean setsTerminationCG(ThreadInfo tiCurrent) {
        return setSingleBlockingCG(TERMINATE, tiCurrent);
    }

    @Override
    public boolean setsJoinCG(ThreadInfo tiCurrent, ThreadInfo tiJoin, long timeout) {
        return setSingleBlockingCG(JOIN, tiCurrent);
    }

    @Override
    public boolean setsRescheduleCG(ThreadInfo ti, String reason) {
        return setSingleNonBlockingCG(reason, ti);
    }

    @Override
    public boolean setsPostFinalizeCG(ThreadInfo tiFinalizer) {
        return setSingleBlockingCG(POST_FINALIZE, tiFinalizer);
    }

    @Override
    public boolean setsParkCG(ThreadInfo ti, boolean isAbsTime, long timeout) {
        return setSingleBlockingCG(PARK, ti);
    }

    @Override
    public boolean setsWaitCG(ThreadInfo ti, long timeout) {
        return setSingleBlockingCG(WAIT, ti);
    }

    @Override
    public boolean setsBlockedThreadCG(ThreadInfo ti, ElementInfo ei) {
        return setSingleBlockingCG(BLOCK, ti);
    }
    //--------------------------------------------------------------------------

    //Multiple Choice
    //CGs are inserted because they are necessary for full schedule exploration
    @Override
    public boolean setsBeginAtomicCG(ThreadInfo ti) {
        return super.setsBeginAtomicCG(ti);
    }
    //--------------------------------------------------------------------------

}
