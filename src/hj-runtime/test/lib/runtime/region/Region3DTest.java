/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lib.runtime.region;

import static edu.rice.hj.Module0.launchHabaneroApp;

import edu.rice.hj.api.HjRegion3D;
import edu.rice.hj.api.HjSuspendable;
import hj.runtime.region.RegionFactory;

/**
 *
 * @author kristophermiles
 */
public class Region3DTest {

    public static void main(String[] args) {
        launchHabaneroApp(new HjSuspendable() {
            @Override
            public void run() {
                HjRegion3D testRegion = RegionFactory.new3D(0, 10, 0, 5, 0, 20);
                System.out.println("Running 3d Region test.");
                //Guarentee correct region creation.
                assert (testRegion.rank() == 3) : "Invalid rank on 3d creation.";

                assert (testRegion.lowerLimits()[0] == 0) : "Invalid lower limit.";
                assert (testRegion.upperLimits()[0] == 10) : "Invalid upper limit.";
                assert (testRegion.lowerLimits()[1] == 0) : "Invalid lower limit.";
                assert (testRegion.upperLimits()[1] == 5) : "Invalid upper limit.";
                assert (testRegion.lowerLimits()[1] == 0) : "Invalid lower limit.";
                assert (testRegion.upperLimits()[1] == 20) : "Invalid upper limit.";

                assert (testRegion.toLinearIndex(0, 0, 0) == 0) : "Invalid linear index.";

                assert (testRegion.numElements() == 231) : "Incorrect number of elements.";
            }
        });
    }
}
