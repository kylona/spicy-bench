package edu.rice.hj;

import edu.rice.hj.api.HjProcedureInt1D;
import edu.rice.hj.api.HjProcedureInt2D;
import edu.rice.hj.api.HjProcedureInt3D;
import edu.rice.hj.api.HjSuspendingProcedureInt1D;
import edu.rice.hj.api.HjRegion1D;
import edu.rice.hj.api.HjRegion2D;
import edu.rice.hj.api.HjRegion3D;
import edu.rice.hj.api.HjRunnable;
import edu.rice.hj.api.HjSuspendingProcedureInt2D;
import edu.rice.hj.api.HjSuspendingProcedureInt3D;
import edu.rice.hj.api.SuspendableException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kristophermiles
 */
public final class HjRegionHelper {

    public static void forasyncSusp(HjRegion1D hjRegion, HjSuspendingProcedureInt1D body) {
        if(!hj.lang.Runtime.isWithinIsolated()){
            throw new java.lang.IllegalStateException ("Asynchronous task may not be created inside an isolated block!");
        }
        int[] startIndex = hjRegion.toRegionIndex(0);
        int[] endIndex = hjRegion.toRegionIndex(hjRegion.numElements() - 1);

        int start = startIndex[0];
        int end = endIndex[0];

        for (int p0 = start; p0 <= end; p0++) {
            final int i = p0;
            Module1.async(new HjRunnable() {
                @Override
                public void run() {
                    try {
                        body.apply(i);
                    } catch (SuspendableException ex) {
                        Logger.getLogger(HjRegionHelper.class.getName()).log(Level.SEVERE, null, ex);
                        ex.printStackTrace();
                    }
                }
            });
        }
    }

    public static void forasyncSusp(HjRegion2D hjRegion, HjSuspendingProcedureInt2D body) {
        if(!hj.lang.Runtime.isWithinIsolated()){
            throw new java.lang.IllegalStateException ("Asynchronous task may not be created inside an isolated block!");
        }
        int[] start = hjRegion.toRegionIndex(0);
        int[] end = hjRegion.toRegionIndex(hjRegion.numElements() - 1);

        int s0 = start[0];
        int e0 = end[0];
        int s1 = start[1];
        int e1 = end[1];

        for (int p0 = s0; p0 <= e0; p0++) {
            for (int p1 = s1; p1 <= e1; p1++) {
                final int i = p0;
                final int j = p1;
                Module1.async(new HjRunnable() {
                    @Override
                    public void run() {
                        try {
                            body.apply(i, j);
                        } catch (SuspendableException ex) {
                            Logger.getLogger(HjRegionHelper.class.getName()).log(Level.SEVERE, null, ex);
                            ex.printStackTrace();
                        }
                    }
                });
            }
        }
    }

    public static void forasyncSusp(HjRegion3D hjRegion, HjSuspendingProcedureInt3D body) {
        if(!hj.lang.Runtime.isWithinIsolated()){
            throw new java.lang.IllegalStateException ("Asynchronous task may not be created inside an isolated block!");
        }
        int[] start = hjRegion.toRegionIndex(0);
        int[] end = hjRegion.toRegionIndex(hjRegion.numElements() - 1);

        int s0 = start[0];
        int e0 = end[0];
        int s1 = start[1];
        int e1 = end[1];
        int s2 = start[2];
        int e2 = end[2];

        for (int p0 = s0; p0 <= e0; p0++) {
            for (int p1 = s1; p1 <= e1; p1++) {
                for (int p2 = s2; p2 <= e2; p2++) {
                    final int i = p0;
                    final int j = p1;
                    final int k = p2;
                    Module1.async(new HjRunnable() {
                        @Override
                        public void run() {
                            try {
                                body.apply(i, j, k);
                            } catch (SuspendableException ex) {
                                Logger.getLogger(HjRegionHelper.class.getName()).log(Level.SEVERE, null, ex);
                                ex.printStackTrace();
                            }
                        }
                    });
                }
            }
        }
    }

    public static void forasync(HjRegion1D hjRegion, HjProcedureInt1D body) {
        HjSuspendingProcedureInt1D newBody = (new HjSuspendingProcedureInt1D() {
            @Override
            public void apply(int paramInt1) {
                body.apply(paramInt1);
            }
        });
        forasyncSusp(hjRegion, newBody);

    }

    public static void forasync(HjRegion2D hjRegion, HjProcedureInt2D body) {
        HjSuspendingProcedureInt2D newBody = (new HjSuspendingProcedureInt2D() {
            @Override
            public void apply(int paramInt1, int paramInt2) {
                body.apply(paramInt1, paramInt2);
            }
        });
        forasyncSusp(hjRegion, newBody);

    }

    public static void forasync(HjRegion3D hjRegion, HjProcedureInt3D body) {
        HjSuspendingProcedureInt3D newBody = (new HjSuspendingProcedureInt3D() {
            @Override
            public void apply(int paramInt1, int paramInt2, int paramInt3) {
                body.apply(paramInt1, paramInt2, paramInt3);
            }
        });
        forasyncSusp(hjRegion, newBody);

    }

    public static void forseq(HjRegion1D hjRegion, HjProcedureInt1D runnable) {
        int[] startIndex = hjRegion.toRegionIndex(0);
        int[] endIndex = hjRegion.toRegionIndex(hjRegion.numElements() - 1);

        int start = startIndex[0];
        int end = endIndex[0];

        for (int p0 = start; p0 <= end; p0++) {
            final int i = p0;
            runnable.apply(i);
        }
    }

    public static void forseq(HjRegion2D hjRegion, HjProcedureInt2D runnable) {
        int[] start = hjRegion.toRegionIndex(0);
        int[] end = hjRegion.toRegionIndex(hjRegion.numElements() - 1);

        int s0 = start[0];
        int e0 = end[0];
        int s1 = start[1];
        int e1 = end[1];

        for (int p0 = s0; p0 <= e0; p0++) {
            for (int p1 = s1; p1 <= e1; p1++) {

                final int i = p0;
                final int j = p1;
                runnable.apply(i, j);

            }
        }
    }

    public static void forseq(HjRegion3D hjRegion, HjProcedureInt3D runnable) {

        int[] start = hjRegion.toRegionIndex(0);
        int[] end = hjRegion.toRegionIndex(hjRegion.numElements() - 1);

        int s0 = start[0];
        int e0 = end[0];
        int s1 = start[1];
        int e1 = end[1];
        int s2 = start[2];
        int e2 = end[2];

        for (int p0 = s0; p0 <= e0; p0++) {
            for (int p1 = s1; p1 <= e1; p1++) {
                for (int p2 = s2; p2 <= e2; p2++) {

                    final int i = p0;
                    final int j = p1;
                    final int k = p2;

                    runnable.apply(i, j, k);

                }
            }
        }
    }

    public static void forseqSusp(HjRegion2D hjRegion, HjSuspendingProcedureInt2D body) throws SuspendableException {
        int[] start = hjRegion.toRegionIndex(0);
        int[] end = hjRegion.toRegionIndex(hjRegion.numElements() - 1);

        int s0 = start[0];
        int e0 = end[0];
        int s1 = start[1];
        int e1 = end[1];

        for (int p0 = s0; p0 <= e0; p0++) {
            for (int p1 = s1; p1 <= e1; p1++) {
                int i = p0;
                int j = p1;
                body.apply(i, j);
            }
        }
    }

    public static void forseqSusp(HjRegion3D hjRegion, HjSuspendingProcedureInt3D body)
            throws SuspendableException {
        int[] start = hjRegion.toRegionIndex(0);
        int[] end = hjRegion.toRegionIndex(hjRegion.numElements() - 1);

        int s0 = start[0];
        int e0 = end[0];
        int s1 = start[1];
        int e1 = end[1];
        int s2 = start[2];
        int e2 = end[2];

        for (int p0 = s0; p0 <= e0; p0++) {
            for (int p1 = s1; p1 <= e1; p1++) {
                for (int p2 = s2; p2 <= e2; p2++) {
                    int i = p0;
                    int j = p1;
                    int k = p2;
                    body.apply(i, j, k);
                }
            }
        }
    }

}
