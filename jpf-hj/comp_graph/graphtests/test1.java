/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphtests;

import static edu.rice.hj.Module0.launchHabaneroApp;
import edu.rice.hj.api.HjSuspendable;
import edu.rice.hj.api.SuspendableException;

/**
 *
 * @author Kris
 */
public class test1 {
    public static void main(final String[] args) {

        launchHabaneroApp(new HjSuspendable() {
            @Override
            public void run() throws SuspendableException {
                System.out.println("This is the simplest possible hj program.");
            }
        });
    }          
}
