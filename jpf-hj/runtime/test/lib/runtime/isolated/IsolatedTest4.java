/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lib.runtime.isolated;

import static edu.rice.hj.Module1.launchHabaneroApp;
import static edu.rice.hj.Module1.async;
import static edu.rice.hj.Module1.finish;
import static edu.rice.hj.Module1.launchHabaneroApp;
import static edu.rice.hj.Module2.isolated;
import edu.rice.hj.api.HjRunnable;
import edu.rice.hj.api.HjSuspendable;

/**
 *
 * @author Peter Anderson <anderson.peter@byu.edu>
 */
public class IsolatedTest4 {

    private volatile static int x = 0;
    private final static int NUM = 3;

    public static void main(String[] args) {
        launchHabaneroApp(new HjSuspendable() {
            public void run() {
                finish(new HjSuspendable() {
                    public void run() {
                        for (int i = 0; i < NUM; i++) {
                            async(new HjRunnable() {
                                public void run() {
                                    isolated(new HjRunnable() {
                                        public void run() {
                                            x++;
                                        }
                                    });
                                }
                            });
                        }
                    }
                });
                assert (x == 3) : "Test Failed";
            }
        });
    }
}
