/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lib.runtime.async;

import edu.rice.hj.Module0;
import static edu.rice.hj.Module0.launchHabaneroApp;
import edu.rice.hj.api.HjRunnable;
import edu.rice.hj.api.HjSuspendable;

/**
 *
 * @author kristophermiles
 */
public class AsyncNBSeq {
     private static volatile int x = 0;
    static {
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
    }
    public static void main(String[] args) {
        launchHabaneroApp(new HjSuspendable() {
            @Override
            public void run() {
                Module0.asyncNbSeq(true,new HjRunnable() {
                    @Override
                    public void run() {
                        assert (x == 0) : "Test Failed";
                        x++;
                    }
                });

                Module0.asyncNbSeq(true,new HjRunnable() {
                    @Override
                    public void run() {
                        assert (x == 1) : "Test Failed";
                        x++;
                    }
                });
            }
        });
    }
}
