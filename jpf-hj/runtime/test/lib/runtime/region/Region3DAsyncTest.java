/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lib.runtime.region;

import edu.rice.hj.Module0;
import edu.rice.hj.api.HjRegion3D;
import edu.rice.hj.api.HjSuspendable;
import edu.rice.hj.api.SuspendableException;

/**
 *
 * @author kristophermiles
 */
public class Region3DAsyncTest {

    public static void main(String[] args) {
        HjRegion3D testRegion = Module0.newRectangularRegion3D(0, 10, 0, 10, 0, 10);
        ThreeDimentionalTestProcedure procedure = new ThreeDimentionalTestProcedure();

        Module0.launchHabaneroApp(new HjSuspendable() {

            @Override
            public void run() throws SuspendableException {
                Module0.finish(new HjSuspendable() {

                    @Override
                    public void run() throws SuspendableException {
                        Module0.forasyncNb(testRegion, procedure);
                    }

                });

                assert (procedure.getResult0() == 55) : "Invalid output result: " + procedure.getResult0();
                assert (procedure.getResult1() == 55) : "Invalid output result: " + procedure.getResult1();
                assert (procedure.getResult2() == 55) : "Invalid output result: " + procedure.getResult2();

            }
        });
    }

}
