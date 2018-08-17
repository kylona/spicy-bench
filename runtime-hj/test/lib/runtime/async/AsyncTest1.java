/*
 * This test create 100 asynchronous tasks that each print to stdout
 * This test should pass if the task ids are not sequential
 *
 */
package lib.runtime.async;

import static edu.rice.hj.Module1.async;
import static edu.rice.hj.Module1.launchHabaneroApp;
import edu.rice.hj.api.HjRunnable;
import edu.rice.hj.api.HjSuspendable;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Peter Anderson <anderson.peter@byu.edu>
 */
public class AsyncTest1 {

    public static void pause(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Logger.getLogger(AsyncTest1.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) {
        launchHabaneroApp(new HjSuspendable() {
            @Override
            public void run() {
                final AsyncTest1 tester = new AsyncTest1();
                for (int i = 0; i < 1000; i++) {
                    final int j = i;
                    async(new HjRunnable() {
                        @Override
                        public void run() {
                            Random random = new Random();
                            long number = Math.abs(random.nextLong() % 50);
                            pause(number);
                            System.out.println("Hello from task " + j);
                        }
                    });
                }
            }
        });
    }
}
