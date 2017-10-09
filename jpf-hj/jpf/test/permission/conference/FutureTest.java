/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package permission.conference;

import static edu.rice.hj.Module1.future;
import static edu.rice.hj.Module1.launchHabaneroApp;
import edu.rice.hj.api.HjFuture;
import edu.rice.hj.api.HjSuspendable;
import static permission.PermissionChecks.acquireR;
import static permission.PermissionChecks.acquireW;
import static permission.PermissionChecks.releaseR;
import static permission.PermissionChecks.releaseW;

/**
 *
 * @author kristophermiles
 */
public class FutureTest {

    public static int[] X;

    public static void main(String[] args) {
        launchHabaneroApp(new HjSuspendable() {
            @Override
            public void run() {

                acquireW(X, 0);
                X = new int[2];
                releaseW(X, 0);
                acquireW(X, 0, 0);
                X[0] = 1;
                releaseW(X, 0, 0);
                acquireW(X, 0, 1);
                X[1] = 2;
                releaseW(X, 0, 1);
                acquireR(X, 0, 0);
                acquireR(X, 0, 1);
                int sum = computeSum(X, 0, X.length - 1);
                releaseR(X, 0, 0);
                releaseR(X, 0, 1);
                System.out.println(sum);
            }
        });

    }

    static int computeSum(int[] X, int lo, int hi) {
        if (lo > hi) {
            return 0;
        } else if (lo == hi) {
            return X[lo];
        } else {
            int mid = (lo + hi) / 2;
            final HjFuture sum1
                    = future(() -> {
                        return computeSum(X, lo, mid);
                    });
            final HjFuture sum2
                    = future(() -> {
                        return computeSum(X, mid + 1, hi);
                    });
            // Parent now waits for the container values
            return (int) sum1.get() + (int) sum2.get();
        }
    }
}
