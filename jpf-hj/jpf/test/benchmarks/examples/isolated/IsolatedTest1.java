/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package benchmarks.examples.isolated;

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
public class IsolatedTest1 {

    private volatile static int i = 0;
    private final static int NUM = 4;

    public static void main(String[] args) {
        launchHabaneroApp(new HjSuspendable() {
            @Override
            public void run() {
                finish(new HjSuspendable() {
                    @Override
                    public void run() {
                        for (int j = 0; j < NUM; j++) {
                            async(new HjRunnable() {
                                @Override
                                public void run() {
                                    isolated(new HjRunnable() {
                                        @Override
                                        public void run() {
                                            i++;
                                        }
                                    });
                                }
                            });
                        }
                    }
                });
            }
        });
    }
}
