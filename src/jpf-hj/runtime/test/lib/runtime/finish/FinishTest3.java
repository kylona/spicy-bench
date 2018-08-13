/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lib.runtime.finish;

import static edu.rice.hj.Module1.async;
import static edu.rice.hj.Module1.finish;
import static edu.rice.hj.Module1.launchHabaneroApp;
import edu.rice.hj.api.HjRunnable;
import edu.rice.hj.api.HjSuspendable;

/**
 *
 * @author Peter Anderson <anderson.peter@byu.edu>
 */
public class FinishTest3 {

    private static volatile int x = 0;

    public static void main(String[] args) {
        launchHabaneroApp(new HjSuspendable() {
            @Override
            public void run() {
                finish(new HjSuspendable() {
                    @Override
                    public void run() {
                        async(new HjRunnable() {
                            @Override
                            public void run() {
                                finish(new HjSuspendable() {
                                    @Override
                                    public void run() {
                                        async(new HjRunnable() {
                                            @Override
                                            public void run() {
                                                x++;
                                            }
                                        });
                                    }
                                });
                                async(new HjRunnable() {
                                    public void run() {
                                        assert (x == 0) : "Test Failed";
                                        x++;
                                    }
                                });
                            }
                        });
                    }
                });
                assert (x == 2) : "Test Failed";
            }
        });
    }
}
