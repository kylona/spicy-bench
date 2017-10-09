/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package permission.phaser;

import static edu.rice.hj.Module1.*;
import static permission.PermissionChecks.*;
import edu.rice.hj.api.*;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 *
 * @author Peter Anderson <anderson.peter@byu.edu>
 */
public class PhaserTest1 {

    public static void main(String[] args) {
        launchHabaneroApp(new HjSuspendable() {
            final Deque deq = new ArrayDeque<>();

            public void run() {
                HjPhaser ph = newPhaser(HjPhaserMode.SIG_WAIT);
                HjPhaserPair pair = new HjPhaserPair(ph, HjPhaserMode.SIG_WAIT);
                asyncPhased(pair, new HjSuspendable() {
                    public void run() {
                        acquireR(deq);
                        System.out.println(deq.contains(4));
                        releaseR(deq);
                        next();
                    }
                });
                asyncPhased(pair, new HjSuspendable() {
                    public void run() {
                        acquireW(deq);
                        deq.add(4);
                        releaseW(deq);
                        next();
                    }
                });
            }
        });
    }
}
