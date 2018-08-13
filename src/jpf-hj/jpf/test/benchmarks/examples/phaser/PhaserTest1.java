/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package benchmarks.examples.phaser;

import static edu.rice.hj.Module1.asyncPhased;
import static edu.rice.hj.Module1.launchHabaneroApp;
import static edu.rice.hj.Module1.newPhaser;
import static edu.rice.hj.Module1.next;
import edu.rice.hj.api.HjPhaser;
import edu.rice.hj.api.HjPhaserMode;
import edu.rice.hj.api.HjPhaserPair;
import edu.rice.hj.api.HjSuspendable;

/**
 *
 * @author Peter Anderson <anderson.peter@byu.edu>
 */
public class PhaserTest1 {

    private static int x = 0;

    public static void main(String[] args) {
        launchHabaneroApp(new HjSuspendable() {
            @Override
            public void run() {
                HjPhaser ph1 = newPhaser(HjPhaserMode.SIG_WAIT);
                HjPhaserPair pair = new HjPhaserPair(ph1, HjPhaserMode.SIG_WAIT);
                asyncPhased(pair, new HjSuspendable() {
                    @Override
                    public void run() {
                        x++;
                        next();
                    }
                });
                asyncPhased(pair, new HjSuspendable() {
                    @Override
                    public void run() {
                        next();
                    }
                });
            }
        });
    }
}
