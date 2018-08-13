package benchmarks.COMP322;

import edu.rice.hj.api.HjRunnable;
import edu.rice.hj.api.HjSuspendable;
import edu.rice.hj.api.SuspendableException;

import java.util.Arrays;
import java.util.List;

import static edu.rice.hj.Module1.*;

/**
 * <p>
 * ForallWithIterable class.</p>
 *
 * @author <a href="http://shams.web.rice.edu/">Shams Imam</a> (shams@rice.edu)
 */
public class ForallWithIterable {

    /**
     * <p>
     * main.</p>
     *
     * @param args an array of {@link String} objects.
     */
    public static void main(final String[] args) {

        final List<Integer> myList = Arrays.asList(1, 2, 3, 4, 5);

        launchHabaneroApp(new HjSuspendable() {
            public void run() {
                try {
                    useFinishAndAsync(myList);
                } catch (SuspendableException e) {
                    System.out.println("Failed test.");
                    e.printStackTrace();
                }

            }

        });

    }

    private static void useFinishAndAsync(final List<Integer> myList) throws SuspendableException {
        finish(new HjSuspendable() {
            @Override
            public void run() {
                for (final Integer item : myList) {
                    async(new HjRunnable() {
                        @Override
                        public void run() {
                            System.out.printf("  Executing item-%d \n", item);
                        }

                    });

                }
            }
        });

    }

}
