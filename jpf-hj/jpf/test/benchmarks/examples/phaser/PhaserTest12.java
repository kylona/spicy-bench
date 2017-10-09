/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package benchmarks.examples.phaser;

import static edu.rice.hj.Module1.*;
import edu.rice.hj.api.*;

/**
 *
 * @author Peter Anderson <anderson.peter@byu.edu>
 */
public class PhaserTest12 {

    public static void main(String[] args) {
        launchHabaneroApp(new HjSuspendable() {
            final Object obj = new Object();

            public void run() {
                HjPhaser ph = newPhaser(HjPhaserMode.SIG_WAIT);
                asyncPhased(ph.inMode(HjPhaserMode.SIG_WAIT), new HjSuspendable() {
                    public void run() {
                        System.out.println("Hello");
                        next();
                        System.out.println("World");
                    }
                });
                asyncPhased(ph.inMode(HjPhaserMode.SIG_WAIT), new HjSuspendable() {
                    public void run() {
                        System.out.println("Goodbye");
                        next();
                        System.out.println("Moon");
                    }
                });
            }
        });
    }
}
