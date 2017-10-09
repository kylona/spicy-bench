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
import static permission.PermissionChecks.acquireW;
import static permission.PermissionChecks.releaseW;

/**
 *
 * @author Peter Anderson <anderson.peter@byu.edu>
 */
public class ConcurrentWriters {

    private static Object x = new Object();
    private static int y = 0;

    public static void main(String[] args) {
        launchHabaneroApp(new HjSuspendable() {
            public void run() {
                finish(new HjSuspendable() {
                    public void run() {
                        async(new HjRunnable() {
                            public void run() {
                                acquireW(x);
                                y++;
                                releaseW(x);
                            }
                        });
                        async(new HjRunnable() {
                            public void run() {
                                acquireW(x);
                                y++;
                                releaseW(x);
                            }
                        });
                    }
                });
            }
        });
    }
}
