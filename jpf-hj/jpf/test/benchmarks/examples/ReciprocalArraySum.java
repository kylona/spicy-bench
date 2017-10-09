package benchmarks.examples;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *
 */
import java.util.Random;

import static edu.rice.hj.Module1.*;
import edu.rice.hj.api.HjRunnable;
import edu.rice.hj.api.HjSuspendable;
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
public class ReciprocalArraySum {

    /**
     * Constant
     * <code>ERROR_MSG="Incorrect argument for array size (shou"{trunked}</code>
     *
     *
     */
    public static final String ERROR_MSG = "Incorrect argument for array size (should be > 0), assuming n = 25,000,000";
    /**
     * Constant <code>DEFAULT_N=100_000_000</code>
     *
     *
     */
    public static final int DEFAULT_N = 1_000;

    private static double sum1;
    private static double sum2;

    public static double parArraySum(final double[] X) {
        // Start of Task T0 (main program)
        //final long startTime = System.nanoTime();
        sum1 = 0;
        sum2 = 0;
        finish(new HjSuspendable() {
            public void run() {
                async(new HjRunnable() {
                    public void run() {
                        acquireR(X, 0, (X.length / 2) - 1);
                        for (int i = 0; i < X.length / 2; i++) {
                            sum1 += 1 / X[i];
                        }
                        releaseR(X, 0, (X.length / 2) - 1);
                    }
                });
                // Compute sum of upper half of array
                acquireR(X, X.length / 2, X.length - 1);
                for (int i = X.length / 2; i < X.length; i++) {
                    sum2 += 1 / X[i];
                }
                releaseR(X, X.length / 2, X.length - 1);
            }
        });
        // Combine sum1 and sum2
        final double sum = sum1 + sum2;
        //final long timeInNanos = System.nanoTime() - startTime;
        //printResults("parArraySum", timeInNanos, sum);
        // Task T0 waits for Task T1 (join)
        return sum;
    }

    public static void main(final String[] argv) {
        launchHabaneroApp(new HjSuspendable() {
            public void run() {
                // Initialization
                int n = DEFAULT_N;
                final double[] X = new double[n];
                final Random myRand = new Random(n);

                for (int i = 0; i < n; i++) {
                    X[i] = myRand.nextInt(n);
                    if (X[i] == 0.0) {
                        i--;
                    }
                }

                for (int numRun = 0; numRun < 5; numRun++) {
                    finish(new HjSuspendable() {
                        public void run() {
                            parArraySum(X);
                        }
                    });
                }
            }
        });
    }
}
