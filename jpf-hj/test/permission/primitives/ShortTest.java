/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package permission.primitives;

import static edu.rice.hj.Module1.async;
import static edu.rice.hj.Module1.finish;
import static edu.rice.hj.Module1.launchHabaneroApp;
import edu.rice.hj.api.HjRunnable;
import edu.rice.hj.api.HjSuspendable;
import static permission.PermissionChecks.acquireR;
import static permission.PermissionChecks.acquireW;
import static permission.PermissionChecks.releaseR;
import static permission.PermissionChecks.releaseW;

/**
 *
 * @author Peter Anderson <anderson.peter@byu.edu>
 */
public class ShortTest {

    private static short x = 0;

    public static void main(String[] args) {
        launchHabaneroApp(new HjSuspendable() {
            public void run() {
                finish(new HjSuspendable() {
                    public void run() {
                        async(new HjRunnable() {
                            public void run() {
                                acquireR(x);
                                releaseR(x);
                            }
                        });
                        async(new HjRunnable() {
                            public void run() {
                                acquireW(x);
                                releaseW(x);
                            }
                        });
                    }
                });
            }
        });
    }
}
