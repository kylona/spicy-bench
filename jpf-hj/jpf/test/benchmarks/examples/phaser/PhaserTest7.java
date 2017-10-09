/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package benchmarks.examples.phaser;

import static edu.rice.hj.Module1.asyncPhased;
import static edu.rice.hj.Module1.finish;
import static edu.rice.hj.Module1.launchHabaneroApp;
import static edu.rice.hj.Module1.newPhaser;
import static edu.rice.hj.Module1.next;
import edu.rice.hj.api.HjPhaser;
import edu.rice.hj.api.HjPhaserMode;
import static edu.rice.hj.api.HjPhaserMode.SIG;
import static edu.rice.hj.api.HjPhaserMode.SIG_WAIT;
import static edu.rice.hj.api.HjPhaserMode.WAIT;
import edu.rice.hj.api.HjPhaserPair;
import edu.rice.hj.api.HjRunnable;
import edu.rice.hj.api.HjSuspendable;

/**
 *
 * @author Peter Anderson <anderson.peter@byu.edu>
 */
public class PhaserTest7 {

    public int x = 0;

    public static void main(String[] args) {
        launchHabaneroApp(new HjSuspendable() {
            @Override
            public void run() {
                final PhaserTest7 tester = new PhaserTest7();
                finish(new HjSuspendable() {
                    @Override
                    public void run() {
                        HjPhaser ph1 = newPhaser(SIG);
                        HjPhaserPair pair = new HjPhaserPair(ph1, HjPhaserMode.WAIT);
                        asyncPhased(pair, new HjSuspendable() {
                            @Override
                            public void run() {
                                finish(new HjSuspendable() {
                                    public void run() {
                                        HjPhaser ph2 = newPhaser(SIG_WAIT);
                                        HjPhaserPair pair2 = new HjPhaserPair(ph2, HjPhaserMode.WAIT);
                                        asyncPhased(pair2, new HjSuspendable() {
                                            @Override
                                            public void run() {
                                                next();
                                            }
                                        });
                                    }
                                });
                                next();
                                tester.x++;
                            }
                        });
                        if (tester.x != 0) {
                            System.err.println("Test Failed");
                            System.exit(1);
                        }
                        next();
                    }
                });
            }
        });
    }
}
