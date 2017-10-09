/*
 * This test create 100 asynchronous tasks that each print to stdout
 * This test should pass if the task ids are not sequential
 *
 */
package benchmarks.examples.async;

import static edu.rice.hj.Module1.async;
import static edu.rice.hj.Module1.launchHabaneroApp;
import edu.rice.hj.api.HjRunnable;
import edu.rice.hj.api.HjSuspendable;

/**
 *
 * @author Peter Anderson <anderson.peter@byu.edu>
 */
public class AsyncTest1 {

    public static void main(String[] args) {
        launchHabaneroApp(new HjSuspendable() {
            @Override
            public void run() {
                for (int i = 0; i < 50; i++) {
                    final int j = i;
                    async(new HjRunnable() {
                        @Override
                        public void run() {
                            System.out.println("Hello from task " + j);
                        }
                    });
                }
            }
        });
    }
}
