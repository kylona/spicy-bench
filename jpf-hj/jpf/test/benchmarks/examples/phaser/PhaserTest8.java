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
import static edu.rice.hj.Module2.isolated;
import edu.rice.hj.api.HjPhaser;
import edu.rice.hj.api.HjPhaserMode;
import static edu.rice.hj.api.HjPhaserMode.SIG_WAIT;
import edu.rice.hj.api.HjPhaserPair;
import edu.rice.hj.api.HjRunnable;
import edu.rice.hj.api.HjSuspendable;

/**
 *
 * @author Peter Anderson <anderson.peter@byu.edu>
 */
public class PhaserTest8 {

    private static final int COUNT = 3;
    private static int total = 0;

    public static void main(String[] args) {
        launchHabaneroApp(new HjSuspendable() {
            @Override
            public void run() {
                finish(new HjSuspendable() {
                    @Override
                    public void run() {
                        HjPhaser ph = newPhaser(SIG_WAIT);
                        for (int i = 0; i < COUNT; i++) {
                            HjPhaserPair pair = new HjPhaserPair(ph, HjPhaserMode.SIG_WAIT);
                            asyncPhased(pair, new HjSuspendable() {
                                @Override
                                public void run() {
                                    next();
                                    isolated(new HjRunnable() {
                                        @Override
                                        public void run() {
                                            total++;
                                        }
                                    });
                                }
                            });
                        }
                        assert (total == 0) : "Test Failed";
                    }
                });
                assert (total == COUNT) : "Test Failed";
            }
        });
    }
}
