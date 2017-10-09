/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lib.runtime.automated;

import static edu.rice.hj.Module1.asyncPhased;
import static edu.rice.hj.Module1.finish;
import static edu.rice.hj.Module1.launchHabaneroApp;
import static edu.rice.hj.Module1.newPhaser;
import static edu.rice.hj.Module1.next;
import static edu.rice.hj.Module2.isolated;
import edu.rice.hj.api.HjPhaser;
import edu.rice.hj.api.HjPhaserMode;
import static edu.rice.hj.api.HjPhaserMode.SIG;
import static edu.rice.hj.api.HjPhaserMode.SIG_WAIT;
import static edu.rice.hj.api.HjPhaserMode.WAIT;
import edu.rice.hj.api.HjPhaserPair;
import edu.rice.hj.api.HjRunnable;
import edu.rice.hj.api.HjSuspendable;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import static junit.framework.Assert.fail;
import benchmarks.examples.phaser.PhaserTest7;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author kristophermiles
 */
public class PhaserTests {

    private static volatile int x = 0;
    private static volatile int y = 0;
    private static long timer;
    private static int FAILTHRESHHOLD = 10;

    public PhaserTests() {
    }

    @Before
    public void setUp() {
        timer = System.currentTimeMillis();
    }

    private int getSecondsElapsed() {
        return (int) (System.currentTimeMillis() - timer) / 1000;
    }

    @Test
    public void basicPhaserTest() {
        AtomicInteger tracker = new AtomicInteger();
        launchHabaneroApp(new HjSuspendable() {
            public void run() {
                HjPhaser ph1 = newPhaser(HjPhaserMode.SIG_WAIT);
                asyncPhased(ph1.inMode(HjPhaserMode.SIG), new HjSuspendable() {
                    public void run() {
                        next();
                        tracker.incrementAndGet();
                    }
                });
                asyncPhased(ph1.inMode(HjPhaserMode.WAIT), new HjSuspendable() {
                    public void run() {
                        next();
                        tracker.incrementAndGet();
                    }
                });
            }
        });
        boolean done = false;
        while (!done) {
            if (tracker.get() == 2) {
                done = true;
            }
            if (!done && getSecondsElapsed() > FAILTHRESHHOLD) {
                done = true;
                fail();
            }
            try {
                Thread.sleep(500);
            } catch (Exception e) {
            }
        }
    }

    @Test
    public void phaserTest2() {
        x = 0;
        launchHabaneroApp(new HjSuspendable() {
            public void run() {
                HjPhaser ph1 = newPhaser(HjPhaserMode.SIG_WAIT);
                asyncPhased(ph1.inMode(HjPhaserMode.WAIT), new HjSuspendable() {
                    public void run() {
                        next();
                        x++;
                    }
                });
            }
        });
        try {
            Thread.sleep(500);
        } catch (Exception e) {
        }
        assertEquals(x, 1);
    }

    @Test
    public void multiplePhaserTest() {
        AtomicInteger tracker = new AtomicInteger();
        launchHabaneroApp(new HjSuspendable() {
            public void run() {
                finish(new HjSuspendable() {
                    public void run() {
                        final HjPhaser ph1 = newPhaser(SIG_WAIT);
                        final HjPhaser ph2 = newPhaser(SIG_WAIT);

                        final List<HjPhaserPair> phList1 = Arrays.asList(
                                ph1.inMode(SIG_WAIT));
                        final List<HjPhaserPair> phList2 = Arrays.asList(
                                ph2.inMode(SIG_WAIT),
                                ph1.inMode(SIG_WAIT));
                        final List<HjPhaserPair> phList3 = Arrays.asList(
                                ph2.inMode(SIG_WAIT));

                        asyncPhased(phList1, new HjSuspendable() {
                            @Override
                            public void run() {
                                // Phase 0
                                next();
                                // Phase 1
                                tracker.getAndIncrement();
                            }
                        });

                        asyncPhased(phList2, new HjSuspendable() {
                            @Override
                            public void run() {
                                // Phase 0
                                next();
                                // Phase 1
                                tracker.getAndIncrement();
                            }
                        });

                        asyncPhased(phList3, new HjSuspendable() {
                            @Override
                            public void run() {
                                // Phase 0
                                next();
                                // Phase 1
                                tracker.getAndIncrement();
                            }
                        });
                    }
                });
            }
        });

        boolean done = false;
        while (!done) {
            if (tracker.get() == 3) {
                done = true;
            }
            if (!done && getSecondsElapsed() > FAILTHRESHHOLD) {
                done = true;
                fail();
            }
            try {
                Thread.sleep(500);
            } catch (Exception e) {
            }
        }
    }

    @Test
    public void incrementPhaserTest() {
        x = 0;
        launchHabaneroApp(new HjSuspendable() {
            public void run() {
                finish(new HjSuspendable() {
                    public void run() {
                        HjPhaser ph1 = newPhaser(SIG_WAIT);
                        asyncPhased(ph1.inMode(SIG_WAIT), new HjSuspendable() {
                            public void run() {
                                assertEquals(0, x);
                                next();
                            }
                        });
                        next();
                        x++;
                    }
                });
            }
        });
    }

    @Test
    public void waitingPhaserTest() {
        y = 0;
        launchHabaneroApp(new HjSuspendable() {
            @Override
            public void run() {
                finish(new HjSuspendable() {
                    @Override
                    public void run() {
                        HjPhaser ph = newPhaser(SIG_WAIT);
                        asyncPhased(ph.inMode(WAIT), new HjSuspendable() {
                            @Override
                            public void run() {
                                next();
                                y++;
                            }
                        });
                    }
                });
            }
        });
        boolean done = false;
        while (!done) {
            if (y == 1) {
                done = true;
            }
            if (!done && getSecondsElapsed() > FAILTHRESHHOLD) {
                done = true;
                fail();
            }
            try {
                Thread.sleep(500);
            } catch (Exception e) {
            }
        }
    }

    @Test
    public void threadPermissionsPhaserTest() {
        launchHabaneroApp(new HjSuspendable() {
            @Override
            public void run() {
                final PhaserTest7 tester = new PhaserTest7();
                finish(new HjSuspendable() {
                    @Override
                    public void run() {
                        HjPhaser ph1 = newPhaser(SIG);
                        asyncPhased(ph1.inMode(WAIT), new HjSuspendable() {
                            @Override
                            public void run() {
                                finish(new HjSuspendable() {
                                    public void run() {
                                        HjPhaser ph2 = newPhaser(SIG_WAIT);
                                        asyncPhased(ph2.inMode(WAIT), new HjSuspendable() {
                                            @Override
                                            public void run() {
                                                next();
                                            }
                                        });
                                    }
                                });
                                next();
                                tester.x++;
                            }
                        });
                        if (tester.x != 0) {
                            fail();
                        }
                        next();
                    }
                });
            }
        });
    }

    @Test
    public void severalPhasersTest() {
        final int COUNT = 10;
        AtomicInteger total = new AtomicInteger();
        launchHabaneroApp(new HjSuspendable() {
            public void run() {
                finish(new HjSuspendable() {
                    public void run() {
                        HjPhaser ph = newPhaser(SIG_WAIT);
                        for (int i = 0; i < COUNT; i++) {
                            asyncPhased(ph.inMode(SIG_WAIT), new HjSuspendable() {
                                public void run() {
                                    next();
                                    isolated(new HjRunnable() {
                                        public void run() {
                                            total.incrementAndGet();
                                        }
                                    });
                                }
                            });
                        }
                        assertEquals(0, total.get());
                    }
                });
                assertEquals(COUNT, total.get());
            }
        });
    }

    @Test
    public void produnceConsumePhaserTest() {
        x = 0;
        y = 0;
        launchHabaneroApp(new HjSuspendable() {
            @Override
            public void run() {
                finish(new HjSuspendable() {
                    @Override
                    public void run() {
                        HjPhaser ph = newPhaser(SIG_WAIT);
                        asyncPhased(ph.inMode(SIG_WAIT), new HjSuspendable() {
                            @Override
                            public void run() {
                                x = 1;
                                assertEquals(1, x);
                                next();
                                y = 0;
                            }
                        });
                        asyncPhased(ph.inMode(SIG_WAIT), new HjSuspendable() {
                            @Override
                            public void run() {
                                y = 1;
                                assertEquals(1, y);
                                next();
                                x = 0;
                            }
                        });
                    }
                });
            }
        });
    }

}
