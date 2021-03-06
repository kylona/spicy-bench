/*
 * This test should deadlock.
 */
package lib.runtime.isolated;

import static edu.rice.hj.Module1.async;
import static edu.rice.hj.Module1.finish;
import static edu.rice.hj.Module1.launchHabaneroApp;
import static edu.rice.hj.Module2.isolated;
import edu.rice.hj.api.HjRunnable;
import edu.rice.hj.api.HjSuspendable;

/**
 *
 * @author Peter Anderson <anderson.peter@byu.edu>
 * 
 * NOTE FROM KRIS: This test performs an illegal behavior as per new async failure standard and thus
 * throws an exception.
 */
public class IsolatedTest2 {

    public static void main(String[] args) {
        launchHabaneroApp(new HjSuspendable() {
            public void run() {
                isolated(new HjRunnable() {
                    public void run() {
                        finish(new HjSuspendable() {
                            public void run() {
                                async(new HjRunnable() {
                                    public void run() {
                                        isolated(new HjRunnable() {
                                            public void run() {
                                                System.err.println("Test Failed");
                                                System.exit(1);
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    }
}
