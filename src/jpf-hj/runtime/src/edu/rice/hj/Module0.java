/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.rice.hj;

import static edu.rice.hj.Module0.forasync;
import static edu.rice.hj.Module0.forasyncChunked;
import edu.rice.hj.api.HjDataDrivenFuture;
import edu.rice.hj.api.HjFinishAccumulator;
import edu.rice.hj.api.HjFuture;
import edu.rice.hj.api.HjMetrics;
import edu.rice.hj.api.HjOperator;
import edu.rice.hj.api.HjPhaser;
import edu.rice.hj.api.HjPhaserMode;
import edu.rice.hj.api.HjPhaserPair;
import edu.rice.hj.api.HjPlace;
import edu.rice.hj.api.HjProcedure;
import edu.rice.hj.api.HjProcedureInt1D;
import edu.rice.hj.api.HjProcedureInt2D;
import edu.rice.hj.api.HjProcedureInt3D;
import edu.rice.hj.api.HjRegion1D;
import edu.rice.hj.api.HjRegion2D;
import edu.rice.hj.api.HjRegion3D;
import edu.rice.hj.api.HjRunnable;
import edu.rice.hj.api.HjSuspendable;
import edu.rice.hj.api.HjSuspendingCallable;
import edu.rice.hj.api.HjSuspendingProcedure;
import edu.rice.hj.api.HjSuspendingProcedureInt1D;
import edu.rice.hj.api.HjSuspendingProcedureInt2D;
import edu.rice.hj.api.HjSuspendingProcedureInt3D;
import edu.rice.hj.api.SuspendableException;
import hj.lang.DataDrivenFuture;
import hj.lang.FinishAccumulatorException;
import hj.runtime.phasers.Phaser;
import edu.rice.hj.api.HjPoint;
import hj.runtime.region.PointFactory;
import hj.runtime.region.RegionFactory;
import hj.runtime.wsh.Activity;
import hj.runtime.wsh.FinishAccumulator;
import hj.runtime.wsh.SuspendableActivity;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kristophermiles
 */
public class Module0 {

    protected static AtomicBoolean runtimeInitialized = new AtomicBoolean(false);
    protected static AtomicInteger numPlaces = new AtomicInteger();
    public static final double BUILD_VER = 0.1;

    public void printBuildInfo(PrintStream outputStream) {
        outputStream.println("HJ Model-checking verification library, developed at BYU.");
        outputStream.println("Build Version: " + BUILD_VER);
    }

    public static void launchHabaneroApp(HjSuspendable suspendable) {
        runtimeInitialized.getAndSet(true);
        SuspendableActivity s_activity = new SuspendableActivity(suspendable, true);
        s_activity.start();
    }

    public static void launchHabaneroApp(HjSuspendable suspendable, Runnable callback) {
        runtimeInitialized.getAndSet(true);
        SuspendableActivity s_activity = new SuspendableActivity(suspendable, true);
        try {
            s_activity.start();
        } finally {
            callback.run();
        }
    }

    public static void finish(HjSuspendable runnable) {
        Activity activity = (Activity) Thread.currentThread();
        activity.startFinish();
        try {
            runnable.run();
        } catch (SuspendableException ex) {
            System.err.println("Caught Suspendable Exception");
            ex.printStackTrace();
        }
        activity.stopFinish();
    }

    public static void initalizeHabanero() {
        HjSuspendable emptyTask = new HjSuspendable() {

            @Override
            public void run() throws SuspendableException {

            }
        };
        launchHabaneroApp(emptyTask);
    }

    public static void finalizeHabanero() {

    }

    public static void emergencyShutdown(Throwable cause) {
        cause.printStackTrace();
        System.exit(1);
    }

    public static int numWorkerThreads() {
        throw new UnsupportedOperationException();
    }

    public static void asyncNb(HjRunnable runnable) {
        if(!hj.lang.Runtime.isWithinIsolated()){
            throw new java.lang.IllegalStateException ("Asynchronous task may not be created inside an isolated block!");
        }
        Activity activity = new Activity(false, runnable, null);
        activity.start();
    }

    public static void asyncNbSeq(boolean sequentalize, HjRunnable runnable) {
        asyncNb(runnable);
    }

    public static void asyncNbSeq(boolean sequentialize, HjRunnable seqRunnable, HjRunnable parRunnable) {
        HjSuspendable cast = new HjSuspendable() {

            @Override
            public void run() {
                seqRunnable.run();
            }
        };

        finish(cast);
        asyncNb(parRunnable);
    }

    public static <T> void forseqNb(Iterable<T> iterable, final HjProcedure<T> body) {
        Iterator<T> stuff = iterable.iterator();
        while (stuff.hasNext()) {
            body.apply(stuff.next());
        }
    }

    public static <T> void forallNb(Iterable<T> iterable, final HjProcedure<T> body) throws SuspendableException {
        HjSuspendable cast = new HjSuspendable() {

            @Override
            public void run() {
                Iterator<T> stuff = iterable.iterator();
                while (stuff.hasNext()) {
                    body.apply(stuff.next());
                }
            }
        };

        finish(cast);
    }

    public static void forseqNb(int startInc, int endInc, HjProcedure<Integer> body) {
        Module1.async(new HjRunnable() {
            @Override
            public void run() {
                for (int i = startInc; i < endInc; i++) {
                    body.apply(i);
                }
            }
        });
    }

    public static <T> void forasyncNb(Iterable<T> iterable, HjProcedure<T> body) {
        HjSuspendable cast = new HjSuspendable() {

            @Override
            public void run() {
                Iterator<T> stuff = iterable.iterator();
                while (stuff.hasNext()) {
                    HjRunnable statement = new HjRunnable() {

                        @Override
                        public void run() {
                            body.apply(stuff.next());
                        }
                    };

                    Module1.async(statement);
                }
            }
        };

        finish(cast);
    }

    public static HjRegion1D newRectangularRegion1D(int pMinInc, int pMaxInc) {
        return RegionFactory.new1D(pMinInc, pMaxInc);
    }

    public static void forallNb(int startInc, final int endInc, final HjProcedure<Integer> body) throws SuspendableException {
        forseqNb(startInc, endInc, body);
    }

    public static void forasyncNb(int startInc, int endInc, HjProcedure<Integer> body) {

        for (int i = startInc; i < endInc; i++) {
            final int value = i;
            HjRunnable statement = new HjRunnable() {

                @Override
                public void run() {
                    body.apply(value);
                }
            };

            Module1.async(statement);
        }
    }

    public static void forallNbChunked(int startInc, int endInc, HjProcedure<Integer> body) throws SuspendableException {
        HjSuspendable cast = new HjSuspendable() {

            @Override
            public void run() {
                for (int i = startInc; i < endInc; i++) {
                    body.apply(i);

                }
            }
        };

        finish(cast);
    }

    public static int computeDefaultChunkSize(int startInc, int endinc) {
        throw new UnsupportedOperationException("This operation is a stub, yo.");
    }

    public static void forallNbChunked(int startInc, final int endInc, final int chunkSize, final HjProcedure<Integer> body) throws SuspendableException {
        forallNbChunked(startInc, endInc, body);
    }

    public static void forasyncNbChunked(int startInc, int endInc, final int chunkSize, final HjProcedure<Integer> body) {
        forasyncNb(startInc, endInc, body);
    }

    public static void forasyncNbChunked(int startInc, int endInc, HjProcedure<Integer> body) {
        forasyncNb(startInc, endInc, body);

    }

    public static <V> HjFuture<V> futureNb(HjSuspendingCallable<V> callable) {
        return Module1.future(callable);
    }

    public static <V> HjFuture<V> futureNbSeq(boolean sequentialize, HjSuspendingCallable<V> callable) {
        return Module1.future(callable);
    }

    public static <V> HjFuture<V> futureNbSeq(boolean sequentialize, HjSuspendingCallable<V> seqCallable, HjSuspendingCallable<V> parCallable) {
        Module1.future(seqCallable);
        return Module1.future(parCallable);
    }

    public static <V> HjDataDrivenFuture<V> newDDF() {
        return new DataDrivenFuture();
    }

    public static <V> HjDataDrivenFuture<V> newDDF(boolean allowDuplicates) {
        return new DataDrivenFuture();
    }

    public static <V> HjDataDrivenFuture<V> newDataDrivenFuture() {
        return new DataDrivenFuture();
    }

    public static <V> HjDataDrivenFuture<V> newDataDrivenFuture(boolean allowDuplicatePuts) {
        return new DataDrivenFuture();
    }

    public static <T> void asyncNbAwait(HjFuture<T> f1, HjSuspendable runnable) {
        Module1.asyncAwait(f1, runnable);
    }

    public static <T> void asyncNbAwait(List<HjFuture<T>> dependences, HjSuspendable runnable) {
        Module1.asyncAwait(dependences, runnable);
    }

    public static <T> void asyncNbAwait(HjFuture<T> f1, HjFuture<T> f2, HjSuspendable runnable) {
        Module1.asyncAwait(f1, f2, runnable);
    }

    public static <T> void asyncNbAwait(HjFuture<T> f1, HjFuture<T> f2, HjFuture<T> f3, HjSuspendable runnable) {
        Module1.asyncAwait(f1, f2, f3, runnable);
    }

    public static <T, V> HjFuture<V> futureNbAwait(HjFuture<T> f1, HjSuspendingCallable<V> callable) {
        return Module1.futureAwait(f1, callable);
    }

    public static <T, V> HjFuture<V> futureNbAwait(List<HjFuture<T>> dependences, HjSuspendingCallable<V> callable) {
        return Module1.futureAwait(dependences, callable);
    }

    public static <T, V> HjFuture<V> futureNbAwait(HjFuture<T> f1, HjFuture<T> f2, HjSuspendingCallable<V> callable) {
        return Module1.futureAwait(f1, f2, callable);
    }

    public static <T, V> HjFuture<V> futureNbAwait(HjFuture<T> f1, HjFuture<T> f2, HjFuture<T> f3, HjSuspendingCallable<V> callable) {
        return Module1.futureAwait(f1, f2, f3, callable);
    }

    public static boolean[] waitAll(HjFuture[] futures) {
        boolean[] bools = new boolean[futures.length];
        for (int i = 0; i < futures.length; i++) {
            futures[i].get();
            bools[i] = (!futures[i].failed());
        }
        return bools;
    }

    public static boolean[] waitAny(HjFuture[] futures) {
        boolean[] bools = new boolean[futures.length];
        while (true) {
            for (int i = 0; i < futures.length; i++) {
                if (futures[i].resolved()) {
                    bools[i] = true;
                    return bools;
                }
            }
            boolean allFailed = true;
            for (HjFuture future : futures) {
                if (future.failed() == false) {
                    allFailed = false;
                    break;
                }
            }
            if (allFailed) {
                return bools;
            }
        }
    }

    public static HjFinishAccumulator newFinishAccumulator(HjOperator ope, Class type) {
        try {
            return new FinishAccumulator(ope, type);
        } catch (FinishAccumulatorException ex) {
            Logger.getLogger(Module0.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        }
        return null;
    }

    public static void finish(HjFinishAccumulator f1, HjSuspendable suspendable) throws SuspendableException {
        throw new UnsupportedOperationException("Finish accumulators are not yet supported.");

    }

    public static void finish(List<HjFinishAccumulator> accumulators, HjSuspendable suspendable) throws SuspendableException {
        throw new UnsupportedOperationException("Finish accumulators are not yet supported.");
    }

    public static void finish(HjFinishAccumulator f1, HjFinishAccumulator f2, HjSuspendable suspendable) throws SuspendableException {
        throw new UnsupportedOperationException("Finish accumulators are not yet supported.");
    }

    public static void finish(HjFinishAccumulator f1, HjFinishAccumulator f2, HjFinishAccumulator f3, HjSuspendable suspendable) throws SuspendableException {
        throw new UnsupportedOperationException("Finish accumulators are not yet supported.");
    }

    public static void asyncPhased(HjPhaserPair phaserPair, HjSuspendable suspendable) {
        if(!hj.lang.Runtime.isWithinIsolated()){
            throw new java.lang.IllegalStateException ("Asynchronous task may not be created inside an isolated block!");
        }        
        HjRunnable runnable = new HjRunnable() {

            @Override
            public void run() {
                try {
                    suspendable.run();
                } catch (SuspendableException ex) {
                    Logger.getLogger(Module0.class.getName()).log(Level.SEVERE, null, ex);
                    ex.printStackTrace();
                }
            }
        };

        LinkedList<HjPhaserPair> phasers = new LinkedList<>();
        phasers.add(phaserPair);
        Activity activity = new Activity(false, runnable, phasers);
        activity.start();
    }

    public static void asyncPhased(HjPhaserPair phaserPair1, HjPhaserPair phaserPair2, HjSuspendable suspendable) {
        if(!hj.lang.Runtime.isWithinIsolated()){
            throw new java.lang.IllegalStateException ("Asynchronous task may not be created inside an isolated block!");
        }
        HjRunnable runnable = new HjRunnable() {

            @Override
            public void run() {
                try {
                    suspendable.run();
                } catch (SuspendableException ex) {
                    Logger.getLogger(Module0.class.getName()).log(Level.SEVERE, null, ex);
                    ex.printStackTrace();
                }
            }
        };

        LinkedList<HjPhaserPair> phasers = new LinkedList<>();
        phasers.add(phaserPair2);
        phasers.add(phaserPair1);
        Activity activity = new Activity(false, runnable, phasers);
        activity.start();
    }

    public static void asyncPhased(List<HjPhaserPair> phaserList, HjSuspendable suspendable) {
        if(!hj.lang.Runtime.isWithinIsolated()){
            throw new java.lang.IllegalStateException ("Asynchronous task may not be created inside an isolated block!");
        }
        HjRunnable runnable = new HjRunnable() {

            @Override
            public void run() {
                try {
                    suspendable.run();
                } catch (SuspendableException ex) {
                    Logger.getLogger(Module0.class.getName()).log(Level.SEVERE, null, ex);
                    ex.printStackTrace();
                }
            }
        };

        Activity activity = new Activity(false, runnable, phaserList);
        activity.start();
    }

    public static void forallPhased(int startInc, final int endInc, final HjSuspendingProcedure<Integer> body) throws SuspendableException {
        if(!hj.lang.Runtime.isWithinIsolated()){
            throw new java.lang.IllegalStateException ("Asynchronous task may not be created inside an isolated block!");
        }
        finish(new HjSuspendable() {
            public void run() throws SuspendableException {
                Phaser ph;
                ph = new Phaser();

                List phList = Arrays.asList(ph);

                Module0.forasyncPhased(startInc, endInc, phList, body);
            }
        });
    }

    public static HjPhaser newPhaser(HjPhaserMode mode) {
        return new Phaser(mode);
    }

    private static void forasyncPhased(int startInc, int endInc, List phList, HjSuspendingProcedure<Integer> body) {
        throw new UnsupportedOperationException("Advanced phaser operations are not supported yet.");
    }

    public static HjPhaser newPhaser(HjPhaserMode mode, int time) {
        return newPhaser(mode);
    }

    public static <T> void forallPhased(Iterable<T> iterable, final HjSuspendingProcedure<T> body) throws SuspendableException {
        throw new UnsupportedOperationException("Advanced phaser operations are not supported yet.");
    }

    public static <T> void forasyncPhased(Iterable<T> iterable, List<HjPhaserPair> phaserList, HjSuspendingProcedure<T> body) {
        throw new UnsupportedOperationException("Advanced phaser operations are not supported yet.");
    }

    public static void forasyncPhased(int startInc, int endInc, HjSuspendingProcedure<Integer> body) {
        throw new UnsupportedOperationException("Advanced phaser operations are not supported yet.");
    }

    public static void next() {
        Activity activity = (Activity) Thread.currentThread();
        activity.doNext();
    }

    public static void signal() {
        Activity activity = (Activity) Thread.currentThread();
        activity.doSignal();
    }

    public static void doWait() {
        Activity activity = (Activity) Thread.currentThread();
        activity.doWait();
    }

    public static void doWork(long n) {
    }

    public static HjMetrics abstractMetrics() {
        System.out.println("Model checking library does not support HjMetrics.");
        return new HjMetrics() {

            @Override
            public long totalWork() {
                return 0;
            }

            @Override
            public long criticalPathLength() {
                return 0;
            }

            @Override
            public double idealParallelism() {
                return 0;
            }
        };
    }

    public static void dumpEventLog(PrintStream stream) {
        stream.println("Model checking library does not support event logs!");
    }

    public static HjPoint newPoint(int[] values) {

        return PointFactory.point(values);
    }

    public static edu.rice.hj.api.HjRegion3D newRectangularRegion3D(int pMinInc0, int pMaxInc0, int pMinInc1, int pMaxInc1, int pMinInc2, int pMaxInc2) {

        return RegionFactory.new3D(pMinInc0, pMaxInc0, pMinInc1, pMaxInc1, pMinInc2, pMaxInc2);
    }

    public static List<HjRegion1D> group(HjRegion1D hjRegion, int processorGrid) {

        return RegionFactory.groupRegion(hjRegion, processorGrid);
    }

    public static HjRegion1D myGroup(int groupId, HjRegion1D hjRegion, int groupSize) {

        return RegionFactory.myGroup(groupId, hjRegion, groupSize);
    }

    public static List<HjRegion2D> group(HjRegion2D hjRegion, int processorGrid0, int processorGrid1) {

        return RegionFactory.groupRegion(hjRegion, processorGrid0, processorGrid1);
    }

    public static HjRegion2D myGroup(int groupId0, int groupId1, HjRegion2D hjRegion, int groupSize0, int groupSize1) {

        return RegionFactory.myGroup(groupId0, groupId1, hjRegion, groupSize0, groupSize1);
    }

    public static void forseqNb(edu.rice.hj.api.HjRegion1D hjRegion, HjProcedureInt1D body) {
        HjRegionHelper.forseq(hjRegion, body);
    }

    public static void forallNb(edu.rice.hj.api.HjRegion1D hjRegion, final HjProcedureInt1D body)
            throws SuspendableException {
        HjSuspendable hjSuspendable = new HjSuspendable() {
            public void run() throws SuspendableException {
                forasync(hjRegion, body);
            }
        };
        finish(hjSuspendable);
    }

    public static void forasync(HjRegion1D hjRegion, HjProcedureInt1D body) {
        HjRegionHelper.forasync(hjRegion, body);
    }

    public static void forallNbChunked(edu.rice.hj.api.HjRegion1D hjRegion, final HjProcedureInt1D body)
            throws SuspendableException {
        HjSuspendable hjSuspendable = new HjSuspendable() {
            public void run() throws SuspendableException {
                forasyncChunked(hjRegion, body);
            }
        };
        finish(hjSuspendable);
    }

    public static void forasyncChunked(HjRegion1D hjRegion, HjProcedureInt1D body) {
        HjSuspendingProcedureInt1D proc = new HjSuspendingProcedureInt1D() {
            @Override
            public void apply(int paramInt1) {
                body.apply(paramInt1);
            }
        };
        Module1.forasync(hjRegion, proc);
    }

    public static void forseqNb(int s0, int e0, int s1, int e1, HjProcedureInt2D body) {
        HjRegion2D hjRegion = RegionFactory.new2D(s0, e0, s1, e1);
        forseqNb(hjRegion, body);
    }

    public static edu.rice.hj.api.HjRegion2D newRectangularRegion2D(int pMinInc0, int pMaxInc0, int pMinInc1, int pMaxInc1) {
        return RegionFactory.new2D(pMinInc0, pMaxInc0, pMinInc1, pMaxInc1);
    }

    public static void forseqNb(HjRegion2D hjRegion, HjProcedureInt2D body) {
        HjRegionHelper.forseq(hjRegion, body);
    }

    public static void forallNb(int s0, int e0, int s1, int e1, HjProcedureInt2D body) throws SuspendableException {

        HjRegion2D hjRegion = newRectangularRegion2D(s0, e0, s1, e1);
        forallNb(hjRegion, body);
    }

    public static void forallNb(HjRegion2D hjRegion, final HjProcedureInt2D body) throws SuspendableException {
        HjSuspendable hjSuspendable = new HjSuspendable() {
            public void run() throws SuspendableException {
                Module0.forasyncNb(hjRegion, body);
            }
        };
        finish(hjSuspendable);
    }

    public static void forasyncNb(HjRegion2D hjRegion, HjProcedureInt2D body) {
        HjRegionHelper.forasync(hjRegion, body);
    }

    public static void forasyncNb(int s0, int e0, int s1, int e1, HjProcedureInt2D body) {
        edu.rice.hj.api.HjRegion2D hjRegion = newRectangularRegion2D(s0, e0, s1, e1);
        forasyncNb(hjRegion, body);
    }

    public static void forallNbChunked(int s0, int e0, int s1, int e1, HjProcedureInt2D body)
            throws SuspendableException {
        edu.rice.hj.api.HjRegion2D hjRegion = newRectangularRegion2D(s0, e0, s1, e1);
        forallNbChunked(hjRegion, body);
    }

    public static void forallNbChunked(edu.rice.hj.api.HjRegion2D hjRegion, final HjProcedureInt2D body)
            throws SuspendableException {
        HjSuspendable hjSuspendable = new HjSuspendable() {
            public void run() throws SuspendableException {
                forasyncNbChunked(hjRegion, body);
            }
        };
        finish(hjSuspendable);
    }

    public static void forasyncNbChunked(HjRegion2D hjRegion, HjProcedureInt2D body) {
        HjSuspendingProcedureInt2D proc = new HjSuspendingProcedureInt2D() {
            @Override
            public void apply(int paramInt1, int paramInt2) {
                body.apply(paramInt1, paramInt2);
            }
        };
        Module1.forasync(hjRegion, proc);
    }

    public static void forseqNb(edu.rice.hj.api.HjRegion3D hjRegion, HjProcedureInt3D body) {
        HjRegionHelper.forseq(hjRegion, body);
    }

    public static void forallNb(edu.rice.hj.api.HjRegion3D hjRegion, final HjProcedureInt3D body)
            throws SuspendableException {
        HjSuspendable hjSuspendable = new HjSuspendable() {
            public void run() throws SuspendableException {
                forasyncNb(hjRegion, body);
            }
        };
        finish(hjSuspendable);
    }

    public static void forasyncNb(HjRegion3D hjRegion, HjProcedureInt3D body) {
        HjRegionHelper.forasync(hjRegion, body);
    }

    public static void forallNbChunked(HjRegion3D hjRegion, final HjProcedureInt3D body)
            throws SuspendableException {
        HjSuspendable hjSuspendable = new HjSuspendable() {
            public void run() throws SuspendableException {
                forasyncNbChunked(hjRegion, body);
            }
        };
        finish(hjSuspendable);
    }

    public static void forasyncNbChunked(HjRegion3D hjRegion, HjProcedureInt3D body) {
        HjSuspendingProcedureInt3D proc = new HjSuspendingProcedureInt3D() {
            @Override
            public void apply(int paramInt1, int paramInt2, int paramInt3) {
                body.apply(paramInt1, paramInt2, paramInt3);
            }
        };
        Module1.forasync(hjRegion, proc);
    }

    public static int numPlaces() {
        return numPlaces.get();
    }

    public static HjPlace here() {
        throw new UnsupportedOperationException("Hj Places not supported in verification library!");
    }

    public static HjPlace place(int id) {
        throw new UnsupportedOperationException("Hj Places not supported in verification library!");
    }

    public static void asyncNbAt(HjPlace place, HjRunnable runnable) {
        throw new UnsupportedOperationException("Hj Places not supported in verification library!");
    }
}
