package lib.runtime.automated;

import static edu.rice.hj.Module1.async;
import static edu.rice.hj.Module1.finish;
import static edu.rice.hj.Module1.launchHabaneroApp;
import static edu.rice.hj.Module2.isolated;
import edu.rice.hj.api.HjRunnable;
import edu.rice.hj.api.HjSuspendable;
import static junit.framework.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author kristophermiles
 */
public class IsolatedTests {

    private static long timer;
    private volatile static int i;
    private final static int NUM = 100;
    private final static int FAILTHRESHHOLD = 10;

    public IsolatedTests() {

    }

    @Before
    public void setUp() {
        timer = System.currentTimeMillis();
    }

    private int getSecondsElapsed() {
        return (int) (System.currentTimeMillis() - timer) / 1000;
    }

    @Test
    public void basicIsolatedTest() {
        i = 0;
        launchHabaneroApp(new HjSuspendable() {
            @Override
            public void run() {
                finish(new HjSuspendable() {
                    @Override
                    public void run() {
                        for (int j = 0; j < NUM; j++) {
                            async(new HjRunnable() {
                                @Override
                                public void run() {
                                    isolated(new HjRunnable() {
                                        @Override
                                        public void run() {
                                            i++;
                                        }
                                    });
                                }
                            });
                        }
                    }
                });
                assertEquals(i, NUM);
            }
        });
    }

    @Test
    public void nestedIsolatedTest() {
        i = 0;
        launchHabaneroApp(new HjSuspendable() {
            @Override
            public void run() {
                isolated(new HjRunnable() {
                    @Override
                    public void run() {
                        isolated(new HjRunnable() {
                            @Override
                            public void run() {
                                i = 1;
                            }
                        });
                    }
                });
            }
        });

        boolean done = false;
        while (!done) {
            if (i == 1) {
                done = true;
            }
            if (!done && getSecondsElapsed() > FAILTHRESHHOLD) {
                done = true;
                fail();
            }
        }
    }

    @Test
    public void complexNestingIsolatedTest() {
        i = 0;
        launchHabaneroApp(new HjSuspendable() {
            public void run() {
                finish(new HjSuspendable() {
                    public void run() {
                        for (int k = 0; k < NUM; k++) {
                            async(new HjRunnable() {
                                public void run() {
                                    isolated(new HjRunnable() {
                                        public void run() {
                                            i++;
                                        }
                                    });
                                }
                            });
                        }
                    }
                });
                assertEquals(i, NUM);
            }
        });
    }

}
