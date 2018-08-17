package benchmarks.COMP322;

import edu.rice.hj.Module2;
import edu.rice.hj.api.HjRunnable;
import edu.rice.hj.api.HjSuspendable;
import static edu.rice.hj.Module1.*;
import static permission.PermissionChecks.*;

/**
 * <p>
 * IntegerCounterIsolated class.</p>
 *
 * @author <a href="http://shams.web.rice.edu/">Shams Imam</a> (shams@rice.edu)
 */
public class IntegerCounterIsolated {

    // Can also use atomic variables instead of isolated
    private final int[] counter = {0};

    /**
     * <p>
     * main.</p>
     *
     * @param args an array of {@link String} objects.
     */
    public static void main(final String[] args) {

        launchHabaneroApp(new HjSuspendable() {
            public void run() {
                final IntegerCounterIsolated anObj = new IntegerCounterIsolated();
                finish(new HjSuspendable() {
                    public void run() {
                        for (int i = 0; i < 3; i++) {
                            async(new HjRunnable() {
                                public void run() {
                                    anObj.foo();
                                }
                            });
                            async(new HjRunnable() {
                                public void run() {
                                    anObj.bar();
                                }
                            });
                            async(new HjRunnable() {
                                public void run() {
                                    anObj.foo();
                                }
                            });
                        }
                    }
                });
                System.out.println("Counter = " + anObj.counter());
            }
        });
    }

    private int counter() {
        int val;
        acquireR(counter, 0);
        val = counter[0];
        releaseR(counter, 0);
        return val;
    }

    /**
     * <p>
     * foo.</p>
     */
    public void foo() {
        Module2.isolated(new HjRunnable() {
            public void run() {
                acquireW(counter, 0);
                counter[0]++;
                releaseW(counter, 0);
            }
        });
    }

    /**
     * <p>
     * bar.</p>
     */
    public void bar() {
        Module2.isolated(new HjRunnable() {
            public void run() {
                acquireW(counter, 0);
                counter[0]--;
                releaseW(counter, 0);
            }

        });
    }
}
