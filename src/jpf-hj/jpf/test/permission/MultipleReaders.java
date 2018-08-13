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
public class MultipleReaders {

    private final static int TASKS = 10;
    private final static int NUMS = 1000;

    public static void main(String[] args) {
        launchHabaneroApp(new HjSuspendable() {
            public void run() {
                final Deque deq = new ArrayDeque<>();
                for (int i = 0; i < NUMS; i++) {
                    deq.add(i);
                }
                finish(new HjSuspendable() {
                    public void run() {
                        for (int i = 0; i < TASKS; i++) {
                            final int ii = i;
                            async(new HjRunnable() {
                                public void run() {
                                    acquireR(deq);
                                    System.out.println(deq.contains(ii));
                                    releaseR(deq);
                                }
                            });
                        }
                    }
                });
            }
        });
    }
}
