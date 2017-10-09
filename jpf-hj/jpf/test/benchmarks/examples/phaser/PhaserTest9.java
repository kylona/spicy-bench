/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package benchmarks.examples.phaser;

import static edu.rice.hj.Module1.*;
import edu.rice.hj.api.HjPhaser;
import edu.rice.hj.api.HjPhaserMode;
import static edu.rice.hj.api.HjPhaserMode.*;
import edu.rice.hj.api.HjPhaserPair;
import edu.rice.hj.api.HjRunnable;
import edu.rice.hj.api.HjSuspendable;

/**
 *
 * @author Peter Anderson <anderson.peter@byu.edu>
 */
public class PhaserTest9 {

    private static final String message = "HELLO";
    private static final char[] bufferOne = {
        'H', 'E', 'L', 'L', 'O'
    };
    private static char[] bufferTwo = new char[5];

    public static void consume(char[] buffer) {
        for (char c : buffer) {
            System.out.print(c);
        }
        System.out.println();
    }

    public static void produce(char[] buffer) {
        assert (buffer != null && buffer.length == message.length());
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = message.charAt(i);
        }
    }

    public static char[] toggle(char[] buffer) {
        if (buffer == bufferOne) {
            return bufferTwo;
        } else {
            return bufferOne;
        }
    }

    public static void main(String[] args) {
        launchHabaneroApp(new HjSuspendable() {
            public void run() {
                finish(new HjSuspendable() {
                    public void run() {
                        final HjPhaser ph1 = newPhaser(SIG_WAIT);
                        HjPhaserPair pair = new HjPhaserPair(ph1, HjPhaserMode.SIG_WAIT);
                        asyncPhased(pair, new HjSuspendable() {
                            @Override
                            public void run() {
                                char[] buffer = bufferTwo;
                                while (true) {
                                    next();
                                    produce(buffer);
                                    buffer = toggle(buffer);
                                }
                            }
                        });

                        asyncPhased(pair, new HjSuspendable() {
                            @Override
                            public void run() {
                                char[] buffer = bufferOne;
                                while (true) {
                                    next();
                                    consume(buffer);
                                    buffer = toggle(buffer);
                                }
                            }
                        });
                    }
                });
            }
        });
    }
}
