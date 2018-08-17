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
public class IsolatedTest3 {

    public int x = 0;

    public static void main(String[] args) {

        final IsolatedTest3 ist3 = new IsolatedTest3();

        launchHabaneroApp(new HjSuspendable() {
            public void run() {

                async(new HjRunnable() {
                    public void run() {
                        isolated(new HjRunnable() {
                            public void run() {
                                acquireW(ist3);
                                ist3.x = 1;
                                releaseW(ist3);
                            }
                        });
                    }
                });

                async(new HjRunnable() {
                    public void run() {
                        isolated(new HjRunnable() {
                            public void run() {
                                acquireR(ist3);
                                if (ist3.x == 0) {
                                    System.out.println("No worries.");
                                } else {
                                    // Add a new variable here that races
                                    // with the other task. The non-protected 
                                    // race only takes place on the else branch.
                                    // No isolated here.
                                    assert (false);
                                }
                                releaseR(ist3);
                            }
                        });
                    }
                });
            }
        });
    }
}
