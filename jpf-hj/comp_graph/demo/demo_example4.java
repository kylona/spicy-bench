/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demo;

import static edu.rice.hj.Module1.async;
import static edu.rice.hj.Module1.finish;
import static edu.rice.hj.Module1.launchHabaneroApp;
import edu.rice.hj.api.HjRunnable;
import edu.rice.hj.api.HjSuspendable;
import edu.rice.hj.api.SuspendableException;

/**
 *
 * @author kristophermiles
 */
public class demo_example4 {

    public static void main(final String[] args) {
        launchHabaneroApp(new HjSuspendable() {
            @Override
            public void run() throws SuspendableException {
                finish(new HjSuspendable() {
                    @Override
                    public void run() {
                        async(new HjRunnable() {
                            @Override
                            public void run() {

                            }
                        });
                    }
                });
            }
        });
    }
}
