package benchmarks.COMP322;

import edu.rice.hj.api.HjFuture;
import edu.rice.hj.api.HjRunnable;
import edu.rice.hj.api.HjSuspendable;
import static edu.rice.hj.Module1.*;
import edu.rice.hj.api.HjSuspendingCallable;
import static permission.PermissionChecks.*;

/**
 * PipelineWithFutures --- Computes (x + 1)^2 using futures
 * <p>
 * The purpose of this example is to illustrate abstract metrics while using
 * futures. It computes (x + 1)^2 using a pipeline to compute the individual
 * fragments x ^ 2, 2 * x, and 1 in left-to-right evaluation order. The final
 * stage of the pipeline prints the result
 *
 * @author <a href="http://shams.web.rice.edu/">Shams Imam</a> (shams@rice.edu)
 */
public class PipelineWithFutures {

    private static final int[] INPUTS = {1, 2, 3, 4, 5/*, 6, 7, 8, 9*/};

    /**
     * <p>
     * main.</p>
     *
     * @param args an array of {@link String} objects.
     */
    public static void main(final String[] args) {
        launchHabaneroApp(new HjSuspendable() {
            public void run() {
                finish(new HjSuspendable() {
                    @Override
                    public void run() {
                        acquireR(INPUTS, 0);//, INPUTS.length-1);
                        for (int i = 0; i < INPUTS.length; i++) {
//                            acquireR(INPUTS, i);
                            pipeline(new Data(INPUTS[i], 0));
//                            acquireR(INPUTS, i);
                        }
                        releaseR(INPUTS, 0);//, INPUTS.length-1);
                    }

                });
            }
        });
    }

    private static void pipeline(final Data data) {

        final HjFuture<Data> firstFuture = future(new HjSuspendingCallable<Data>() {
            @Override
            public Data call() {
                final Data dataItem = data;
                final int input = dataItem.input;

                final int result = dataItem.accum + (input * input);
                return new Data(input, result);
            }
        });

        // computes 2 * x + accum
        final HjFuture<Data> secondFuture = future(new HjSuspendingCallable() {

            @Override
            public Data call() {
                final Data dataItem = firstFuture.get();
                final int input = dataItem.input;

                final int result = dataItem.accum + (2 * input);
                return new Data(input, result);
            }
        });

        // computes 1 + accum
        final HjFuture<Data> thirdFuture = future(new HjSuspendingCallable() {

            @Override
            public Object call() {
                final Data dataItem = secondFuture.get();
                final int input = dataItem.input;

                final int result = dataItem.accum + 1;
                return new Data(input, result);
            }
        });

        // prints the result
        async(new HjRunnable() {
            @Override
            public void run() {
                final Data dataItem = thirdFuture.get();
                final int input = dataItem.input;
                final int result = dataItem.accum;
                System.out.printf(" f(%3d) = %3d \n", input, result);
            }

        });
    }

    private static class Data {

        final int input;
        final int accum;

        private Data(final int input, final int accum) {
            this.input = input;
            this.accum = accum;
        }
    }

}
