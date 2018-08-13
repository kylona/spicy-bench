package demo;

/*
 * Isolated - with GPR annotations
 */
import static edu.rice.hj.Module1.async;
import static edu.rice.hj.Module1.finish;
import static edu.rice.hj.Module1.launchHabaneroApp;
import static edu.rice.hj.Module2.isolated;
import edu.rice.hj.api.HjSuspendable;
import edu.rice.hj.api.HjRunnable;
import edu.rice.hj.api.SuspendableException;
import static permission.PermissionChecks.*;

public class demo_example3 {

    public static int[] a = new int[1];

    public static void main(final String[] args) {

        launchHabaneroApp(new HjSuspendable() {
            @Override
            public void run() throws SuspendableException {

                finish(new HjSuspendable() {
                    @Override
                    public void run() {

                        async(new HjRunnable() {
                            @Override
                            public void run() {
                                isolated(new HjRunnable() {
                                    public void run() {
                                        acquireW(a, 0);
                                        a[0] = 2;
                                        releaseW(a, 0);
                                        acquireR(a, 0);
                                        System.out.println("a[0] = " + a[0]);
                                        releaseR(a, 0);
                                    }
                                });
                            }
                        });

                        async(new HjRunnable() {
                            @Override
                            public void run() {
                                isolated(new HjRunnable() {
                                    public void run() {
                                        acquireW(a, 0);
                                        a[0] = 5;
                                        releaseW(a, 0);
                                        acquireR(a, 0);
                                        System.out.println("a[0] = " + a[0]);
                                        releaseR(a, 0);
                                    }
                                });
                            }
                        });
                    }
                });
                acquireR(a, 0);
                System.out.println("value of a = " + a[0]);
                releaseR(a, 0);
            }
        });
    }
}
