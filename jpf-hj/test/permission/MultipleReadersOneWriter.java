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
import java.util.ArrayDeque;
import java.util.Deque;
import static permission.PermissionChecks.*;

/**
 *
 * @author Peter Anderson <anderson.peter@byu.edu>
 */
public class MultipleReadersOneWriter {

    private final static int TASKS = 50;

    public static void main(String[] args) {
        launchHabaneroApp(new HjSuspendable() {
            public void run() {
                final Deque deq = new ArrayDeque<>();
                finish(new HjSuspendable() {
                    public void run() {
                        for (int i = 0; i < TASKS; i++) {
                            async(new HjRunnable() {
                                public void run() {
                                    acquireR(deq);
                                    System.out.println(deq.contains(10));
                                    releaseR(deq);
                                }
                            });
                        }
                        async(new HjRunnable() {
                            public void run() {
                                acquireW(deq);
                                deq.add(10);
                                releaseW(deq);
                            }
                        });
                    }
                });
            }
        });
    }
}
