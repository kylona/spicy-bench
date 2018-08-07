
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *
 */
import edu.rice.hj.api.HjFuture;
import java.util.Random;
import static edu.rice.hj.Module1.*;
import edu.rice.hj.api.HjSuspendable;
import edu.rice.hj.api.HjSuspendingCallable;
import static permission.PermissionChecks.*;

/**
 * ReciprocalArraySum --- Computing the sum of reciprocals of array elements
 * with 2-way parallelism
 * <p>
 * The goal of this example program is to create an array of n random int's, and
 * compute the sum of their reciprocals in two ways: 1) Sequentially in method
 * seqArraySum() 2) In parallel using two tasks in method parArraySum() The
 * profitability of the parallelism depends on the size of the array and the
 * overhead of async creation.
 * <p>
 * Your assignment is to use two-way parallelism in method parArraySum() to
 * obtain a smaller execution time than seqArraySum()
 *
 * @author Vivek Sarkar (vsarkar@rice.edu)
 *
 */
public class ReciprocalArraySumFutures {

    /**
     * Constant <code>ERROR_MSG="Incorrect argument for array size"</code>
     *
     */
    public static final String ERROR_MSG = "Incorrect argument for array size (should be > 0), assuming n = 25,000,000";
    /**
     * Constant <code>DEFAULT_N=100_000_000</code>
     *
     */
    public static final int DEFAULT_N = 1_000;

    /**
     * <p>
     * parArraySumFutures.</p>
     *
     * @param X an array of double.
     * @return a double.
     *
     */
    public static double parArraySumFutures(final double[] X) {

        final HjFuture<Double> sum1 = future(new HjSuspendingCallable<Double>() {
            @Override
            public Double call() {
                // Return sum of lower half of array
                double lowerSum = 0;
                acquireR(X, 0, (X.length / 2) - 1);
                for (int i = 0; i < X.length / 2; i++) {
                    lowerSum += 1 / X[i];
                }
                releaseR(X, 0, (X.length / 2) - 1);
                return lowerSum;
            }
        });
        final HjFuture<Double> sum2 = future(() -> {
            // Return sum of upper half of array
            double upperSum = 0;
            acquireR(X, X.length / 2, X.length - 1);
            for (int i = X.length / 2; i < X.length; i++) {
                upperSum += 1 / X[i];
            }
            releaseR(X, X.length / 2, X.length - 1);
            return upperSum;
        });

        // Combine sum1 and sum2
        final double sum = sum1.get() + sum2.get();
        return sum;
    }

    public static void main(final String[] argv) {
        launchHabaneroApp(new HjSuspendable() {
            @Override
            public void run() {
                // Initialization
                int n = DEFAULT_N;
                double[] X = new double[n];
                final Random myRand = new Random(n);

                for (int i = 0; i < n; i++) {
                    X[i] = myRand.nextInt(n);
                    if (X[i] == 0.0) {
                        i--;
                    }
                }

                finish(new HjSuspendable() {
                    @Override
                    public void run() {
                        for (int numRun = 0; numRun < 5; numRun++) {
                            System.out.printf("Run %d\n", numRun);
                            parArraySumFutures(X);
                        }
                    }
                });
            }
        });
    }
}
