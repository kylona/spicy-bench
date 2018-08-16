/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lib.runtime.region;

import static edu.rice.hj.Module0.launchHabaneroApp;
import edu.rice.hj.api.HjRegion1D;
import edu.rice.hj.api.HjSuspendable;
import hj.runtime.region.RegionFactory;
import java.util.Iterator;

/**
 *
 * @author kristophermiles
 */
public class Region1DTest {

    static {
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
    }

    public static void main(String[] args) {
        launchHabaneroApp(new HjSuspendable() {
            @Override
            public void run() {
                HjRegion1D testRegion = RegionFactory.new1D(0, 100);

                //Guarentee correct region creation.
                assert (testRegion.rank() == 1) : "Invalid rank on 1d creation.";
                assert (testRegion.lowerLimits()[0] == 0) : "Invalid lower limit.";
                assert (testRegion.upperLimits()[0] == 100) : "Invalid upper limit.";
                assert (testRegion.toLinearIndex(0) == 0) : "Invalid linear index.";
                assert (testRegion.numElements() == 101) : "Incorrect number of elements.";
                assert (testRegion.iterator().getClass() == Iterator.class) : "Inccorrect iterator creation";

            }
        });
    }
}
