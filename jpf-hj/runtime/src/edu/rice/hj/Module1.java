package edu.rice.hj;

import edu.rice.hj.api.HjRegion3D;
import edu.rice.hj.api.HjRegion2D;
import edu.rice.hj.api.HjRegion1D;
import static edu.rice.hj.Module0.finish;
import edu.rice.hj.api.*;
import hj.lang.Future;
import hj.runtime.region.*;

import hj.runtime.wsh.Activity;
import hj.runtime.wsh.SuspendableActivity;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Peter Anderson <anderson.peter@byu.edu>
 */
public class Module1 extends Module0 {

    public static void async(HjRunnable runnable) {
        if(!hj.lang.Runtime.isWithinIsolated()){
            throw new java.lang.IllegalStateException ("Asynchronous task may not be created inside an isolated block!");
        }
        Activity activity = new Activity(false, runnable, null);
        activity.start();
    }

    public static <V> HjFuture<V> future(HjSuspendingCallable<V> callable) {
        Future future = new Future(callable);
        future.start();
        return future;
    }

    public static <T> void forseq(Iterable iterable, HjProcedure<T> body) throws SuspendableException {
        HjSuspendable hjSuspendable = new HjSuspendable() {
            @Override
            public void run() throws SuspendableException {
                Module1.forseq(iterable, body);
            }
        };
        hjSuspendable.run();
    }

    public static <T> void forAll(Iterable<T> iterable, HjProcedure<T> body) throws SuspendableException {
        final Iterable<T> iterable_f = iterable;
        final HjProcedure<T> body_f = body;
        finish(new HjSuspendable() {
            @Override
            public void run() {
                for (T t : iterable_f) {
                    final T tt = t;
                    async(new HjRunnable() {
                        @Override
                        public void run() {
                            body_f.apply(tt);
                        }
                    });
                }
            }
        });
    }

    public static <T> void forAsync(Iterable<T> iterable, HjSuspendingProcedure<T> body) {
        final HjSuspendingProcedure<T> body_f = body;
        for (T t : iterable) {
            final T tt = t;
            async(new HjRunnable() {
                @Override
                public void run() {
                    try {
                        body_f.apply(tt);
                    } catch (SuspendableException ex) {
                        Logger.getLogger(Module1.class.getName()).log(Level.SEVERE, null, ex);
                        ex.printStackTrace();
                    }
                }
            });
        }
    }

    public static void forseq(int start, int end, HjSuspendingProcedure<Integer> runnable) {
        HjRegion1D region = RegionFactory.new1D(start, end);
        HjRegionHelper.forseq(region, new HjProcedureInt1D() {
            @Override
            public void apply(int i) {
                try {
                    runnable.apply(i);
                } catch (SuspendableException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    public static void forAll(int startInc, int endInc, HjSuspendingProcedure<Integer> body) throws SuspendableException {
        final int startInc_f = startInc;
        final int endInc_f = endInc;
        final HjSuspendingProcedure<Integer> body_f = body;
        finish(new HjSuspendable() {
            @Override
            public void run() {
                for (int k = startInc_f; k <= endInc_f; k++) {
                    final int kk = k;
                    async(new HjRunnable() {
                        @Override
                        public void run() {
                            try {
                                body_f.apply(kk);
                            } catch (SuspendableException ex) {
                                Logger.getLogger(Module1.class.getName()).log(Level.SEVERE, null, ex);
                                ex.printStackTrace();
                            }
                        }
                    });
                }
            }
        });
    }

    public static void forAsync(int startInc, int endInc, HjSuspendingProcedure<Integer> body) {
        final HjSuspendingProcedure<Integer> body_f = body;
        for (int k = startInc; k <= endInc; k++) {
            final int kk = k;
            async(new HjRunnable() {
                @Override
                public void run() {
                    try {
                        body_f.apply(kk);
                    } catch (SuspendableException ex) {
                        Logger.getLogger(Module1.class.getName()).log(Level.SEVERE, null, ex);
                        ex.printStackTrace();
                    }
                }
            });
        }
    }

    public static void forallChunked(int startInc, int endInc, HjSuspendingProcedure<Integer> body) throws SuspendableException {
        forAll(startInc, endInc, body);
    }

    public static void forasyncChunked(int startInc, int endInc, final int chunkSize, final HjSuspendingProcedure<Integer> body) throws SuspendableException {
        forAsync(startInc, endInc, body);
    }

    public static void forasyncChunked(int startInc, int endInc, HjSuspendingProcedure<Integer> body) throws SuspendableException {
        forAsync(startInc, endInc, body);
    }

    public static void forasync(HjRegion1D hjRegion, HjSuspendingProcedureInt1D body) {
        HjRegionHelper.forasyncSusp(hjRegion, body);
    }

    public static void forallChunked(HjRegion1D hjRegion, final HjSuspendingProcedureInt1D body) throws SuspendableException {
        HjRegionHelper.forasyncSusp(hjRegion, body);
    }

    public static void forasyncChunked(HjRegion1D hjRegion, HjSuspendingProcedureInt1D body) throws SuspendableException {
        HjRegionHelper.forasyncSusp(hjRegion, body);
    }

    public static void forseq(int s0, int e0, int s1, int e1, HjSuspendingProcedureInt2D body) throws SuspendableException {
        HjRegion2D hjRegion = newRectangularRegion2D(s0, e0, s1, e1);
        forseq(hjRegion, body);
    }

    public static void forseq(HjRegion2D hjRegion, HjSuspendingProcedureInt2D body) throws SuspendableException {
        HjRegionHelper.forseqSusp(hjRegion, body);
    }

    public static void forall(int s0, int e0, int s1, int e1, HjSuspendingProcedureInt2D body) throws SuspendableException {
        HjRegion2D hjRegion = newRectangularRegion2D(s0, e0, s1, e1);
        forall(hjRegion, body);
    }

    public static void forall(HjRegion2D hjRegion, final HjSuspendingProcedureInt2D body) throws SuspendableException {
        HjSuspendable hjSuspendable = new HjSuspendable() {
            public void run() throws SuspendableException {
                forasync(hjRegion, body);
            }
        };
        finish(hjSuspendable);
    }

    public static void forasync(HjRegion2D hjRegion, HjSuspendingProcedureInt2D body) {
        HjRegionHelper.forasyncSusp(hjRegion, body);
    }

    public static void forasync(int s0, int e0, int s1, int e1, HjSuspendingProcedureInt2D body) {
        HjRegion2D hjRegion = newRectangularRegion2D(s0, e0, s1, e1);
        forasync(hjRegion, body);
    }

    public static void forasyncChunked(int s0, int e0, int s1, int e1, HjSuspendingProcedureInt2D body) throws SuspendableException {
        HjRegion2D hjRegion = newRectangularRegion2D(s0, e0, s1, e1);
        forasync(hjRegion, body);
    }

    public static void forallChunked(int s0, int e0, int s1, int e1, HjSuspendingProcedureInt2D body) throws SuspendableException {
        HjRegion2D hjRegion = newRectangularRegion2D(s0, e0, s1, e1);
        forall(hjRegion, body);
    }

    public static void forallChunked(HjRegion2D hjRegion, final HjSuspendingProcedureInt2D body) throws SuspendableException {
        HjSuspendable hjSuspendable = new HjSuspendable() {
            @Override
            public void run() throws SuspendableException {
                forall(hjRegion, body);
            }
        };
        finish(hjSuspendable);
    }

    public static void forasyncChunked(HjRegion2D hjRegion, HjSuspendingProcedureInt2D body) throws SuspendableException {
        HjRegionHelper.forasyncSusp(hjRegion, body);
    }

    public static void forseq(HjRegion3D hjRegion, HjSuspendingProcedureInt3D body) throws SuspendableException {
        HjRegionHelper.forseqSusp(hjRegion, body);
    }

    public static <T> void forall(HjRegion3D hjRegion, HjSuspendingProcedureInt3D body) {
        HjSuspendable hjSuspendable = new HjSuspendable() {
            @Override
            public void run() throws SuspendableException {
                HjSuspendingProcedureInt3D procedure = (HjSuspendingProcedureInt3D) body;
                forasync(hjRegion, procedure);
            }
        };

        finish(hjSuspendable);
    }

    public static void forasync(HjRegion3D hjRegion, HjSuspendingProcedureInt3D body) {
        HjRegionHelper.forasyncSusp(hjRegion, body);
    }

    public static void forallChunked(HjRegion3D hjRegion, final HjSuspendingProcedureInt3D body) throws SuspendableException {
        forall(hjRegion, body);
    }

    public static void forasyncChunked(HjRegion3D hjRegion, HjSuspendingProcedureInt3D body) throws SuspendableException {
        HjRegionHelper.forasyncSusp(hjRegion, body);
    }

    public static <T> void asyncAwait(HjFuture<T> f1, HjSuspendable suspendable) {
        if(!hj.lang.Runtime.isWithinIsolated()){
            throw new java.lang.IllegalStateException ("Asynchronous task may not be created inside an isolated block!");
        }
        HjRunnable runnable = new HjRunnable() {

            @Override
            public void run() {
                try {
                    suspendable.run();
                } catch (SuspendableException ex) {
                    Logger.getLogger(Module1.class.getName()).log(Level.SEVERE, null, ex);
                    ex.printStackTrace();
                }
            }
        };

        Activity activity = new Activity(false, runnable, null);
        HjFuture<T> futures[] = new HjFuture[1];
        futures[0] = f1;

        asyncAwaitBlocker(futures, activity);
    }

    public static <T> void asyncAwait(List<HjFuture<T>> futures, HjSuspendable suspendable) {
        if(!hj.lang.Runtime.isWithinIsolated()){
            throw new java.lang.IllegalStateException ("Asynchronous task may not be created inside an isolated block!");
        }
        
        HjRunnable runnable = new HjRunnable() {

            @Override
            public void run() {
                try {
                    suspendable.run();
                } catch (SuspendableException ex) {
                    Logger.getLogger(Module1.class.getName()).log(Level.SEVERE, null, ex);
                    ex.printStackTrace();
                }
            }
        };

        Activity activity = new Activity(false, runnable, null);
        HjFuture<T> futureArray[] = new HjFuture[futures.size()];

        for (int i = 0; i < futures.size(); i++) {
            futureArray[i] = futures.get(i);
        }

        asyncAwaitBlocker(futureArray, activity);

    }

    public static <T> void asyncAwait(HjFuture<T> f1, HjFuture<T> f2, HjSuspendable suspendable) {
        if(!hj.lang.Runtime.isWithinIsolated()){
            throw new java.lang.IllegalStateException ("Asynchronous task may not be created inside an isolated block!");
        }
        
        HjRunnable runnable = new HjRunnable() {

            @Override
            public void run() {
                try {
                    suspendable.run();
                } catch (SuspendableException ex) {
                    Logger.getLogger(Module1.class.getName()).log(Level.SEVERE, null, ex);
                    ex.printStackTrace();
                }
            }
        };

        Activity activity = new Activity(false, runnable, null);
        HjFuture<T> futures[] = new HjFuture[2];
        futures[0] = f1;
        futures[1] = f2;

        asyncAwaitBlocker(futures, activity);
    }

    public static <T> void asyncAwait(HjFuture<T> f1, HjFuture<T> f2,
            HjFuture<T> f3, HjSuspendable suspendable) {
        if(!hj.lang.Runtime.isWithinIsolated()){
            throw new java.lang.IllegalStateException ("Asynchronous task may not be created inside an isolated block!");
        }
        
        HjRunnable runnable = new HjRunnable() {

            @Override
            public void run() {
                try {
                    suspendable.run();
                } catch (SuspendableException ex) {
                    Logger.getLogger(Module1.class.getName()).log(Level.SEVERE, null, ex);
                    ex.printStackTrace();
                }
            }
        };

        Activity activity = new Activity(false, runnable, null);
        HjFuture<T> futures[] = new HjFuture[3];
        futures[0] = f1;
        futures[1] = f2;
        futures[2] = f3;

        asyncAwaitBlocker(futures, activity);
    }

    private static <T> void asyncAwaitBlocker(HjFuture<T>[] futures, Activity activity) {
        if(!hj.lang.Runtime.isWithinIsolated()){
            throw new java.lang.IllegalStateException ("Asynchronous task may not be created inside an isolated block!");
        }
        final HjFuture<T>[] f_futures = futures;
        final Activity f_activity = activity;

        HjRunnable blocker = new HjRunnable() {
            @Override
            public void run() {
                int i = 0;
                while (i < f_futures.length) {
                    if (f_futures[i].resolved()) {
                        i++;
                    }
                }
                f_activity.start();
            }
        };
        async(blocker);
    }

    public static void asyncSusp(HjSuspendable suspendable) {
        if(!hj.lang.Runtime.isWithinIsolated()){
            throw new java.lang.IllegalStateException ("Asynchronous task may not be created inside an isolated block!");
        }
        SuspendableActivity s_activity
                = new SuspendableActivity(suspendable, false);
        s_activity.start();
    }

    public static <T, V> HjFuture<V> futureAwait(HjFuture<T> f1, HjSuspendingCallable callable) {
        Future future = new Future(callable);
        f1.get();
        future.start();
        return future;
    }

    public static <T, V> HjFuture<V> futureAwait(HjFuture<T> f1, HjFuture<T> f2, HjSuspendingCallable callable) {
        Future future = new Future(callable);
        f1.get();
        f2.get();
        future.start();
        return future;
    }

    public static <T, V> HjFuture<V> futureAwait(HjFuture<T> f1, HjFuture<T> f2,
            HjFuture<T> f3, HjSuspendingCallable callable) {
        Future future = new Future(callable);
        f1.get();
        f2.get();
        f3.get();
        future.start();
        return future;
    }

    public static <T, V> HjFuture<V> futureAwait(List<HjFuture<T>> futures, HjSuspendingCallable callable) {
        Future future = new Future(callable);
        for (HjFuture<T> ddf : futures) {
            ddf.get();
        }
        future.start();
        return future;
    }
}
