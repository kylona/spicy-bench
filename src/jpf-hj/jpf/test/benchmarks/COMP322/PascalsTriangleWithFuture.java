package benchmarks.COMP322;

import edu.rice.hj.api.HjFuture;
import edu.rice.hj.api.HjSuspendable;
import edu.rice.hj.api.SuspendableException;

import static edu.rice.hj.Module1.*;
import static edu.rice.hj.Module1.future;
import edu.rice.hj.api.HjSuspendingCallable;
import static permission.PermissionChecks.*;

/**
 * Pascal's Triangle --- Computes (n C k) using futures
 * <p>
 * The purpose of this example is to illustrate abstract metrics while using
 * futures. C(n, k) = C(n - 1, k - 1) + C(n - 1, k)
 *
 * @author <a href="http://shams.web.rice.edu/">Shams Imam</a> (shams@rice.edu)
 * @author Vivek Sarkar (vsarkar@rice.edu)
 */
public class PascalsTriangleWithFuture {

    private static final boolean DEBUG = false;

    /**
     * <p>
     * main.</p>
     *
     * @param args an array of {@link String} objects.
     */
    public static void main(final String[] args) {

        final int n = args.length > 0 ? Integer.parseInt(args[0]) : 3;
        final int k = args.length > 1 ? Integer.parseInt(args[1]) : (n - 2);

        System.out.println(" N = " + n);
        System.out.println(" K = " + k);

        launchHabaneroApp(new HjSuspendable() {
            @Override
            public void run() {
                finish(new HjSuspendable() {
                    @Override
                    public void run() {
                        int res;
                        try {
                            res = choose(n, k);
                            System.out.println(n + " choose " + k + " = " + res);
                        } catch (SuspendableException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                });
            }

        });
    }

    @SuppressWarnings("unchecked")
    private static int choose(final int N, final int K) throws SuspendableException {

        @SuppressWarnings("unchecked")
        final HjFuture<Integer>[][] choose_N_K = new HjFuture[N + 1][];
        for (int n = 0; n <= N; n++) {
            @SuppressWarnings("unchecked")
            final HjFuture<Integer>[] temp = new HjFuture[K + 1];
            choose_N_K[n] = temp;
        }

        for (int n = 0; n <= N; n++) {
            final int nVal = n;
            for (int k = 0; k <= K; k++) {
                final int kVal = k;

                choose_N_K[nVal][kVal] = future(new HjSuspendingCallable<Integer>() {
                    @Override
                    public Integer call() {
                        if (kVal == 0) {
                            return 1;
                        } else if (nVal == 0) {
                            return 0;
                        } else {
                            HjFuture<Integer>[] leftish = choose_N_K[nVal - 1];
                            HjFuture<Integer>[] rightish = choose_N_K[nVal - 1];
                            acquireR(leftish, kVal - 1);
                            acquireR(rightish, kVal - 1);
                            final HjFuture<Integer> left = choose_N_K[nVal - 1][kVal - 1];
                            final HjFuture<Integer> right = choose_N_K[nVal - 1][kVal - 1];

                            final int leftVal = left.get();
                            final int rightVal = right.get();
                            releaseR(leftish, kVal - 1);
                            releaseR(rightish, kVal - 1);

                            return leftVal + rightVal;
                        }
                    }
                });
            }
        }

        final Integer result = choose_N_K[N][K].get();

        if (DEBUG) {
            for (int n = 0; n <= N; n++) {
                for (int k = 0; k <= K; k++) {
                    System.out.printf("%3d", choose_N_K[n][k].get());
                }
                System.out.println();
            }
        }

        return result;
    }

}
