/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package permission.isolated;

import static edu.rice.hj.Module2.*;
import edu.rice.hj.api.HjRunnable;
import edu.rice.hj.api.HjSuspendable;
import static permission.PermissionChecks.*;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 *
 * @author Peter Anderson <anderson.peter@byu.edu>
 */
public class IsolatedTest1 {

    public static void main(String[] args) {
        launchHabaneroApp(new HjSuspendable() {
            final Deque obj = new ArrayDeque<>();

            public void run() {
                async(new HjRunnable() {
                    public void run() {
                        isolated(new HjRunnable() {
                            public void run() {
                                acquireW(obj);
                                obj.push(1);
                                releaseW(obj);
                            }
                        });
                    }
                });
                async(new HjRunnable() {
                    public void run() {
                        isolated(new HjRunnable() {
                            public void run() {
                                acquireW(obj);
                                obj.add(1);
                                releaseW(obj);
                            }
                        });
                    }
                });
            }
        });
    }
}
