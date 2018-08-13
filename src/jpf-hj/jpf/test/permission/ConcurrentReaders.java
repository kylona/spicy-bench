/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package permission;

import static edu.rice.hj.Module1.async;
import static edu.rice.hj.Module1.finish;
import static edu.rice.hj.Module1.launchHabaneroApp;
import edu.rice.hj.api.HjRunnable;
import edu.rice.hj.api.HjSuspendable;
import static permission.PermissionChecks.acquireR;
import static permission.PermissionChecks.releaseR;

/**
 *
 * @author Peter Anderson <anderson.peter@byu.edu>
 */
public class ConcurrentReaders {

    private static final int y = 0;
    private static final Object x = new Object();

    public static void main(String[] args) {
        launchHabaneroApp(new HjSuspendable() {
            public void run() {
                finish(new HjSuspendable() {
                    public void run() {
                        async(new HjRunnable() {
                            public void run() {
                                acquireR(x);
                                System.out.println("Reading y from task 1");
                                int temp = y;
                                releaseR(x);
                            }
                        });
                        async(new HjRunnable() {
                            public void run() {
                                acquireR(x);
                                System.out.println("Reading y from task 2");
                                int temp = y;
                                releaseR(x);
                            }
                        });
                    }
                });
            }
        });
    }
}
