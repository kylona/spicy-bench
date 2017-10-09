/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphtests;

import static edu.rice.hj.Module0.finish;
import static edu.rice.hj.Module0.launchHabaneroApp;
import static edu.rice.hj.Module1.async;
import edu.rice.hj.api.HjRunnable;
import edu.rice.hj.api.HjSuspendable;
import edu.rice.hj.api.SuspendableException;

/**
 *
 * @author Kris
 */
public class test7 {

    private static final int loops = 250;

    public static void main(final String[] args) {

        launchHabaneroApp(new HjSuspendable() {
            @Override
            public void run() throws SuspendableException {
                finish(new HjSuspendable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < loops; i++) {
                            async(new HjRunnable() {
                                @Override
                                public void run() {
                                    System.out.println("This is a simple HJ program containing two async statements.");
                                }
                            });
                        }
                        System.out.println("This is a basic hj program with another enclosing finish.");
                    }
                });

            }
        });
    }
}
