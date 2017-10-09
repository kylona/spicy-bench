package lib.runtime.automated;

import static edu.rice.hj.Module1.async;
import static edu.rice.hj.Module1.launchHabaneroApp;
import edu.rice.hj.api.HjRunnable;
import edu.rice.hj.api.HjSuspendable;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import lib.runtime.async.AsyncTest1;
import static lib.runtime.async.AsyncTest1.pause;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static permission.PermissionChecks.acquireW;
import static permission.PermissionChecks.releaseW;

/**
 *
 * @author kristophermiles
 */
public class AsyncTests {

    private long timer = 0;
    private static final int TESTS = 1000;
    private static final int FAILTHRESHHOLD = 10;
    private static volatile int tempInt = 0;
    private static volatile int[] x = {0};

    public AsyncTests() {
    }

    @Before
    public void setUp() {
        timer = System.currentTimeMillis();
    }

    @After
    public void tearDown() {
    }

    private int getSecondsElapsed() {
        return (int) (System.currentTimeMillis() - timer) / 1000;
    }

    @Test
    public void tasksRunSeperatelyTest() {
        AtomicInteger successes = new AtomicInteger();

        launchHabaneroApp(new HjSuspendable() {
            @Override
            public void run() {
                final AsyncTest1 tester = new AsyncTest1();
                for (int i = 0; i < TESTS; i++) {
                    final int j = i;
                    async(new HjRunnable() {
                        @Override
                        public void run() {
                            Random random = new Random();
                            long number = Math.abs(random.nextLong() % 50);
                            pause(number);
                            successes.getAndIncrement();
                        }
                    });
                }
            }
        });

        boolean done = false;
        while (!done) {
            if (successes.get() == TESTS) {
                done = true;
            }
            if (!done && getSecondsElapsed() > FAILTHRESHHOLD) {
                done = true;
                fail();
            }
        }
    }

    @Test
    public void volatileRespectedTest() {
        tempInt = 0;
        launchHabaneroApp(new HjSuspendable() {
            @Override
            public void run() {
                async(new HjRunnable() {
                    @Override
                    public void run() {
                        assertFalse((tempInt == 1));
                        tempInt++;
                    }
                });

                async(new HjRunnable() {
                    @Override
                    public void run() {
                        assertFalse((tempInt == 0));
                        tempInt++;
                    }
                });
            }
        });
    }

    @Test
    public void volatileArrayTest() {
        AtomicInteger successes = new AtomicInteger();
        launchHabaneroApp(new HjSuspendable() {
            @Override
            public void run() {
                for (int i = 0; i < TESTS; i++) {
                    async(new HjRunnable() {
                        @Override
                        public void run() {
                            acquireW(x, 0);
                            x[0]++;
                            releaseW(x, 0);
                            successes.incrementAndGet();
                        }
                    });
                }
            }
        });

        boolean done = false;
        while (!done) {
            if (successes.get() == TESTS) {
                done = true;
            }
            if (!done && getSecondsElapsed() > FAILTHRESHHOLD) {
                done = true;
                fail();
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }
        }

    }
}
