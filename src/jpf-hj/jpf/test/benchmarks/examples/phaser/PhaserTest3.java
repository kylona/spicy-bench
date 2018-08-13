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
import static edu.rice.hj.api.HjPhaserMode.SIG_WAIT;
import edu.rice.hj.api.HjPhaserPair;
import edu.rice.hj.api.HjRunnable;
import edu.rice.hj.api.HjSuspendable;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Peter Anderson <anderson.peter@byu.edu>
 */
public class PhaserTest3 {

    public static void main(String[] args) {
        launchHabaneroApp(new HjSuspendable() {
            @Override
            public void run() {
                finish(new HjSuspendable() {
                    @Override
                    public void run() {
                        final HjPhaser ph1 = newPhaser(SIG_WAIT);
                        final HjPhaser ph2 = newPhaser(SIG_WAIT);

                        final List<HjPhaserPair> phList1 = Arrays.asList(
                                ph1.inMode(SIG_WAIT));
                        final List<HjPhaserPair> phList2 = Arrays.asList(
                                ph2.inMode(SIG_WAIT),
                                ph1.inMode(SIG_WAIT));
                        final List<HjPhaserPair> phList3 = Arrays.asList(
                                ph2.inMode(SIG_WAIT));

                        asyncPhased(phList1, new HjSuspendable() {
                            @Override
                            public void run() {
                                // Phase 0
                                next();
                                // Phase 1
                            }
                        });

                        asyncPhased(phList2, new HjSuspendable() {
                            @Override
                            public void run() {
                                // Phase 0
                                next();
                                // Phase 1
                            }
                        });

                        asyncPhased(phList3, new HjSuspendable() {
                            @Override
                            public void run() {
                                // Phase 0
                                next();
                                // Phase 1
                            }
                        });
                    }
                });
            }
        });
    }
}
