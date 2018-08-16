package lib.runtime.automated;

import static edu.rice.hj.Module1.async;
import static edu.rice.hj.Module1.finish;
import static edu.rice.hj.Module1.launchHabaneroApp;
import edu.rice.hj.api.HjRunnable;
import edu.rice.hj.api.HjSuspendable;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author kristophermiles
 */
public class FinishTests {

    private static long timer;
    public static volatile int testInt;
    public static volatile int x;

    public FinishTests() {
    }

    @Before
    public void setUp() {
        timer = System.currentTimeMillis();
    }

    private int getSecondsElapsed() {
        return (int) (System.currentTimeMillis() - timer) / 1000;
    }

    @Test
    public void basicFinishTest() {
        testInt = 0;
        launchHabaneroApp(new HjSuspendable() {
            @Override
            public void run() {
                finish(new HjSuspendable() {
                    @Override
                    public void run() {
                        async(new HjRunnable() {
                            @Override
                            public void run() {
                                testInt++;
                            }
                        });
                    }
                });
                async(new HjRunnable() {
                    @Override
                    public void run() {
                        assertFalse((testInt == 0));
                    }
                });
            }
        });
    }

    @Test
    public void nestedFinishTest() {
        testInt = 0;
        launchHabaneroApp(new HjSuspendable() {
            @Override
            public void run() {
                finish(new HjSuspendable() {
                    @Override
                    public void run() {
                        async(new HjRunnable() {
                            @Override
                            public void run() {
                                async(new HjRunnable() {
                                    @Override
                                    public void run() {
                                        testInt = 1;
                                    }
                                });
                            }
                        });
                    }
                });
                assertFalse((testInt == 0));
            }
        });
    }

    @Test
    public void complexFinishTest() {
        testInt = 0;
        launchHabaneroApp(new HjSuspendable() {
            @Override
            public void run() {
                finish(new HjSuspendable() {
                    @Override
                    public void run() {
                        async(new HjRunnable() {
                            @Override
                            public void run() {
                                finish(new HjSuspendable() {
                                    @Override
                                    public void run() {
                                        async(new HjRunnable() {
                                            @Override
                                            public void run() {
                                                assertEquals(x, 0);
                                                x++;
                                            }
                                        });
                                    }
                                });
                                async(new HjRunnable() {
                                    public void run() {
                                        assertEquals(x, 1);
                                        x++;
                                    }
                                });
                            }
                        });
                    }
                });
                System.out.println("x=" + x);
                assertEquals(x, 2);
            }
        });
    }
}
