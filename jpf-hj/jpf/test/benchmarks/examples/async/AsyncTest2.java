/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package benchmarks.examples.async;

import static edu.rice.hj.Module1.async;
import static edu.rice.hj.Module1.launchHabaneroApp;
import edu.rice.hj.api.HjRunnable;
import edu.rice.hj.api.HjSuspendable;
import static permission.PermissionChecks.*;

/**
 *
 * @author Peter Anderson <anderson.peter@byu.edu>
 */
public class AsyncTest2 {

    private static volatile int[] x = {0};

    public static void main(String[] args) {
        launchHabaneroApp(new HjSuspendable() {
            @Override
            public void run() {
                async(new HjRunnable() {
                    @Override
                    public void run() {
                        acquireW(x, 0);
                        x[0]++;
                        releaseW(x, 0);
                    }
                });

                async(new HjRunnable() {
                    @Override
                    public void run() {
                        acquireW(x, 0);
                        x[0]++;
                        releaseW(x, 0);
                    }
                });
            }
        });
    }
}
