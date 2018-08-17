/*
 * This 
 */
package benchmarks.examples.isolated;

import static edu.rice.hj.Module1.launchHabaneroApp;
import static edu.rice.hj.Module2.isolated;
import edu.rice.hj.api.HjRunnable;
import edu.rice.hj.api.HjSuspendable;

/**
 *
 * @author Peter Anderson <anderson.peter@byu.edu>
 */
public class IsolatedTest3 {

    private static int i = 0;

    public static void main(String[] args) {
        launchHabaneroApp(new HjSuspendable() {
            @Override
            public void run() {
                isolated(new HjRunnable() {
                    @Override
                    public void run() {
                        isolated(new HjRunnable() {
                            @Override
                            public void run() {
                                i = 1;
                            }
                        });
                    }
                });
            }
        });
    }
}
