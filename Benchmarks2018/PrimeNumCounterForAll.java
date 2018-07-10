
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *
 */
import static edu.rice.hj.Module1.*;
import static edu.rice.hj.Module2.isolated;
import edu.rice.hj.api.HjRunnable;
import edu.rice.hj.api.HjSuspendable;
import edu.rice.hj.api.HjSuspendingProcedure;
import edu.rice.hj.api.SuspendableException;
import static permission.PermissionChecks.*;

/**
 *
 * @author Peter Anderson <anderson.peter@byu.edu>
 *
 *
 */
public class PrimeNumCounterForAll {

    private final static int COUNT = 7;
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
            @Override
            public void run() {
                try {
                    forAll(2, COUNT, new HjSuspendingProcedure<Integer>() {
                        @Override
                        public void apply(Integer k) {
                            if (isPrime(k)) {
                                isolated(new HjRunnable() {
                                    @Override
                                    public void run() {
                                        acquireW(primes, 0);
                                        primes[0]++;
                                        acquireW(primes, 0);
                                    }
                                });
                            }
                        }
                    });
                } catch (SuspendableException ex) {
                    System.exit(1);
                }
                int primeVal;
                acquireR(primes, 0);
                primeVal = primes[0];
                releaseR(primes, 0);
                System.out.println("Number of primes less than " + COUNT + " is " + primeVal);
            }
        });
    }
}
