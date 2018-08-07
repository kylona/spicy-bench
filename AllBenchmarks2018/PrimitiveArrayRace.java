/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import static edu.rice.hj.Module1.async;
import static edu.rice.hj.Module1.finish;
import static edu.rice.hj.Module1.launchHabaneroApp;
import edu.rice.hj.api.HjRunnable;
import edu.rice.hj.api.HjSuspendable;

import static permission.PermissionChecks.*;

/**
 *
 * @author Peter Anderson <anderson.peter@byu.edu>
 */
public class PrimitiveArrayRace {

    public static final int SIZE = 3;

    public static void main(String[] args) {
        launchHabaneroApp(new HjSuspendable() {
            public void run() {
                int[] primitives = new int[SIZE];
                finish(new HjSuspendable() {
                    public void run() {
                        async(new HjRunnable() {
                            public void run() {
                                acquireW(primitives, 0, SIZE - 1);
                                for (int i = 0; i < (SIZE / 2) + 1; i++) {
                                    primitives[i] = 1;
                                }
                                releaseW(primitives, 0, SIZE - 1);
                            }
                        });
                        async(new HjRunnable() {
                            public void run() {
                                acquireW(primitives, 0, SIZE - 1);
                                for (int i = (SIZE / 2); i < SIZE; i++) {
                                    primitives[i] = 1;
                                }
                                releaseW(primitives, 0, SIZE - 1);
                            }
                        });
                    }
                });
//                for (int i = 0; i < primitives.length; i++) {
//                    System.out.println(primitives[i]);
//                }
            }
        });
    }
}
