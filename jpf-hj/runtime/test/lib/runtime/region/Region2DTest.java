/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lib.runtime.region;

import static edu.rice.hj.Module0.launchHabaneroApp;
import edu.rice.hj.api.HjRegion2D;
import edu.rice.hj.api.HjSuspendable;
import hj.runtime.region.RegionFactory;

/**
 *
 * @author kristophermiles
 */
public class Region2DTest {

    static {
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
    }

    public static void main(String[] args) {
        launchHabaneroApp(new HjSuspendable() {
            @Override
            public void run() {
                HjRegion2D testRegion = RegionFactory.new2D(0, 10, 0, 20);
                System.out.println("Running 2d Region test.");
                //Guarentee correct region creation.
                assert (testRegion.rank() == 2) : "Invalid rank on 2d creation.";

                assert (testRegion.lowerLimits()[0] == 0) : "Invalid lower limit.";
                assert (testRegion.upperLimits()[0] == 10) : "Invalid upper limit.";
                assert (testRegion.lowerLimits()[1] == 0) : "Invalid lower limit.";
                assert (testRegion.upperLimits()[1] == 20) : "Invalid upper limit.";

                assert (testRegion.toLinearIndex(0, 0) == 0) : "Invalid linear index.";

                assert (testRegion.numElements() == 231) : "Incorrect number of elements.";
            }
        });
    }
}
