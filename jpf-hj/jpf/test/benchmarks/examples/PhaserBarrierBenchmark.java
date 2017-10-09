package benchmarks.examples;

import static edu.rice.hj.Module1.*;
import edu.rice.hj.api.HjPhaser;
import edu.rice.hj.api.HjPhaserMode;
import edu.rice.hj.api.HjSuspendable;
import edu.rice.hj.api.SuspendableException;
import java.io.IOException;

/*
 * To change this license header, choose License Headers in Project Properties.
 package edu.rice.hj.example.primers;

 import edu.rice.hj.api.HjPhaser;
 import edu.rice.hj.api.HjPhaserMode;
 import edu.rice.hj.api.SuspendableException;

 import java.io.IOException;

 import static edu.rice.hj.Module1.*;

 /**
 * <p>PhaserBarrierBenchmark class.</p>
 *
 * @author <a href="http://shams.web.rice.edu/">Shams Imam</a> (shams@rice.edu)
 */
public class PhaserBarrierBenchmark {

    private static final int participants = 4;
    private static final int rounds = 3;

    /**
     * <p>
     * main.</p>
     *
     * @param args an array of {@link String} objects.
     * @throws java.io.IOException if any.
     */
    public static void main(final String[] args) throws IOException {
        // global finish scope
        launchHabaneroApp(new HjSuspendable() {
            public void run() {
                runIteration();
            }
        });
    }

    private static void runIteration() {
        finish(new HjSuspendable() {
            public void run() {
                final HjPhaser ph = newPhaser(HjPhaserMode.SIG_WAIT);
                for (int p = 0; p < participants; p++) {
                    final int id = p;
                    asyncPhased(ph.inMode(HjPhaserMode.SIG_WAIT), () -> {
                        for (int i = 0; i < rounds; i++) {
                            System.out.printf("Participant-%d before barrier-%d. \n", id, i);
                            next();
                            System.out.printf("  Participant-%d after barrier-%d. \n", id, i);
                        }
                    });
                }

                System.out.println("End of runnable body in finish.");
            }
        });
    }
}
