/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lib.runtime.region;

import static edu.rice.hj.Module0.launchHabaneroApp;
import edu.rice.hj.api.HjPoint;
import edu.rice.hj.api.HjSuspendable;
import hj.runtime.region.PointFactory;

/**
 *
 * @author kristophermiles
 */
public class PointTest {

    static {
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
    }

    public static void main(String[] args) {
        launchHabaneroApp(new HjSuspendable() {
            @Override
            public void run() {
                //First, we make sure the point can set correctly.
                HjPoint testPoint = PointFactory.point(5);
                assert (testPoint.rank() == 1) : "Invalid rank on test point.";
                assert (testPoint.get(0) == 5) : "Invalid rank on test point.";

                //Now, we test to make sure the point is equal to itself.
                assert (testPoint.compareTo(testPoint) == 0) : "Invalid compareto result on point.";
                HjPoint testPoint2 = PointFactory.point(1);
                assert (testPoint.compareTo(testPoint2) != 0) : "Invalid compareto result on unequal point.";

            }
        });
    }
}
