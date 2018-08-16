/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package permission.conference;

import static edu.rice.hj.Module1.*;
import edu.rice.hj.api.HjSuspendable;
import java.util.Arrays;
import static permission.PermissionChecks.acquireR;
import static permission.PermissionChecks.acquireW;
import static permission.PermissionChecks.releaseR;

/**
 *
 * @author kristophermiles
 */
public class StringTest {

    public static void main(String[] args) {
        launchHabaneroApp(new HjSuspendable() {
            @Override
            public void run() {
                finish(() -> { // Task T1 owns A
                    int[] A = new int[2]; // ... initialize array A ...
                    acquireW(A, 0, 0);
                    A[0] = 1;
                    releaseW(A, 0, 0);
                    acquireW(A, 0, 1);
                    A[1] = 2;
                    releaseW(A, 0, 0);

                    // create a copy of array A in B
                    int[] B = new int[A.length];
                    acquireR(A, 0, 0);
                    acquireR(A, 0, 1);
                    System.arraycopy(A, 0, B, 0, A.length);
                    releaseR(A, 0, 0);
                    releaseR(A, 0, 1);
                    async(() -> { // Task T2 owns B
                        System.out.println("sum = " + sum(B));
                    });
                    acquireW(A, 0, 0);
                    A[0] = 100;
                    releaseW(A, 0, 0);
                    // ... update Array A ...
                    acquireR(A, 0, 0);
                    acquireR(A, 0, 1);
                    System.out.println(Arrays.toString(A)); //printed by task T1
                    releaseR(A, 0, 0);
                    releaseR(A, 0, 1);
                });
            }

            private void releaseW(int[] A, int i, int i0) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        });

    }

    private static String sum(int[] B) {
        int output = 0;
        for (int i = 0; i < B.length; i++) {
            output += B[i];
        }
        return "" + output;
    }
}
