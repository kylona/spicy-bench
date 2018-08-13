/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package benchmarks.matrix;

import static edu.rice.hj.Module1.*;
import edu.rice.hj.api.*;
import static permission.PermissionChecks.*;

/**
 *
 * @author Peter Anderson <anderson.peter@byu.edu>
 */
public class VectorAdd {

    private final int SIZE = 10;
    private int[] A;
    private int[] B;

    public VectorAdd(int a_val, int b_val) {
        A = new int[SIZE];
        B = new int[SIZE];
        acquireR(A, 0, A.length - 1);
        for (int i = 0; i < A.length; i++) {
            A[i] = a_val;
        }
        releaseR(A, 0, A.length - 1);
        acquireR(B, 0, B.length - 1);
        for (int i = 0; i < B.length; i++) {
            B[i] = b_val;
        }
        releaseR(B, 0, B.length - 1);
    }

    public int[] adder(int start, int end) {
        assert (end > start && end > 0 && start >= 0);
        int[] result = new int[end - start];
        acquireR(A, start, end - 1);
        acquireR(B, start, end - 1);
        for (int i = start; i < end; i++) {
            result[i - start] = A[i] + B[i];
        }
        releaseR(B, start, end - 1);
        releaseR(A, start, end - 1);
        return result;
    }

    public static void main(String[] args) {
        launchHabaneroApp(new HjSuspendable() {
            public void run() {
                VectorAdd obj = new VectorAdd(1, 0);
                finish(() -> {
                    async(() -> {
                        int[] first = obj.adder(0, (obj.SIZE) / 2);
                    });
                    async(() -> {
                        int[] second = obj.adder((obj.SIZE) / 2, obj.SIZE);
                    });
                });
            }
        });
    }
}
