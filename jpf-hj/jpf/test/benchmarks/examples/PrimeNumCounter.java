package benchmarks.examples;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *
 */
import static edu.rice.hj.Module1.async;
import static edu.rice.hj.Module1.finish;
import static edu.rice.hj.Module1.launchHabaneroApp;
import static edu.rice.hj.Module2.isolated;
import edu.rice.hj.api.HjRunnable;
import edu.rice.hj.api.HjSuspendable;
import static permission.PermissionChecks.*;

/**
 *
 * @author Peter Anderson <anderson.peter@byu.edu>
 *
 *
 */
public class PrimeNumCounter {

    private final static int COUNT = 17;
    private static int[] primes = {0};

    public static boolean isPrime(int num) {
        for (int i = 2; i < num; i++) {
            if (num % i == 0) {
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        launchHabaneroApp(new HjSuspendable() {
            public void run() {
                finish(new HjSuspendable() {
                    public void run() {
                        for (int i = 2; i < COUNT; i++) {
                            final int j = i;
                            async(new HjRunnable() {
                                public void run() {
                                    if (isPrime(j)) {
                                        isolated(new HjRunnable() {
                                            public void run() {
                                                acquireW(primes, 0);
                                                primes[0]++;
                                                releaseW(primes, 0);
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    }
                });
                int primeVal;
                acquireR(primes, 0);
                primeVal = primes[0];
                releaseR(primes, 0);
                System.out.println("Number of primes less than " + COUNT + " is " + primeVal);
            }
        });
    }
}
