/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lib.runtime.future;

import static edu.rice.hj.Module1.async;
import static edu.rice.hj.Module1.finish;
import static edu.rice.hj.Module1.future;
import static edu.rice.hj.Module1.launchHabaneroApp;
import edu.rice.hj.api.HjFuture;
import java.util.Random;

/**
 *
 * @author Peter Anderson <anderson.peter@byu.edu>
 */
public class TestOne {

    public static final int DEFAULT_N = 10_000;

    /**
     * <p>
     * parArraySumFutures.</p>
     *
     * @param X an array of double.
     * @return a double.
     *
     */
    public static double parArraySumFutures(final double[] X) {

        final HjFuture<Double> sum1 = future(() -> {
            // Return sum of lower half of array
            double lowerSum = 0;
            for (int i = 0; i < X.length / 2; i++) {
                lowerSum += 1 / X[i];
            }
            return lowerSum;
        });
        final HjFuture<Double> sum2 = future(() -> {
            // Return sum of upper half of array
            double upperSum = 0;
            for (int i = X.length / 2; i < X.length; i++) {
                upperSum += 1 / X[i];
            }
            return upperSum;
        });

        // Combine sum1 and sum2
        final double sum = sum1.get() + sum2.get();
        return sum;
    }

    /**
     * <p>
     * main.</p>
     *
     * @param argv an array of {@link String} objects.
     *
     */
    public static void main(final String[] argv) {
        launchHabaneroApp(() -> {
            // Initialization
            double[] X;
            X = new double[DEFAULT_N];
            final Random myRand = new Random(DEFAULT_N);

            for (int i = 0; i < DEFAULT_N; i++) {
                X[i] = myRand.nextInt(DEFAULT_N);
                if (X[i] == 0.0) {
                    i--;
                }
            }

            finish(() -> {
                parArraySumFutures(X);
            });
        });
    }
}
