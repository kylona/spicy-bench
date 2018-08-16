/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lib.runtime.region;

import edu.rice.hj.Module0;
import edu.rice.hj.api.HjRegion1D;
import edu.rice.hj.api.HjSuspendable;
import edu.rice.hj.api.SuspendableException;

/**
 *
 * @author kristophermiles
 */
public class Region1DForallTest {

    static {
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
    }

    public static void main(String[] args) {

        HjRegion1D testRegion = Module0.newRectangularRegion1D(0, 10);
        OneDimentionalTestProcedure procedure = new OneDimentionalTestProcedure();

        Module0.launchHabaneroApp(new HjSuspendable() {

            @Override
            public void run() throws SuspendableException {

                Module0.forallNb(testRegion, procedure);
                assert (procedure.getResult() == 55) : "Invalid output result: " + procedure.getResult();

            }
        });
    }
}
