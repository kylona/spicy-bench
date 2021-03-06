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
import static edu.rice.hj.api.HjPhaserMode.SIG;
import static edu.rice.hj.api.HjPhaserMode.SIG_WAIT;
import static edu.rice.hj.api.HjPhaserMode.WAIT;
import edu.rice.hj.api.HjRunnable;
import edu.rice.hj.api.HjSuspendable;

/**
 *
 * @author Peter Anderson <anderson.peter@byu.edu>
 */
public class PhaserTest11 {

    private final static int COUNT = 5;
    private static int x = 0;

    public static void main(String[] args) {
        launchHabaneroApp(new HjSuspendable() {
            @Override
            public void run() {
                finish(new HjSuspendable() {
                    @Override
                    public void run() {
                        HjPhaser ph = newPhaser(SIG_WAIT);
                        asyncPhased(ph.inMode(SIG_WAIT), new HjSuspendable() {
                            @Override
                            public void run() {
                                for (int i = 0; i < COUNT; i++) {
                                    next();
                                }
                            }
                        });
                        asyncPhased(ph.inMode(SIG), new HjSuspendable() {
                            @Override
                            public void run() {
                                for (int i = 0; i < COUNT; i++) {
                                    next();
                                }
                            }
                        });
                        asyncPhased(ph.inMode(WAIT), new HjSuspendable() {
                            @Override
                            public void run() {
                                for (int i = 0; i < COUNT; i++) {
                                    next();
                                }
                            }
                        });
                    }
                });
            }
        });
    }
}
