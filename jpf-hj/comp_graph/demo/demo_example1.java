package demo;

/*
 * Basic example - async and finish
 * isolated 
 * No Permission annotations
 */
import static edu.rice.hj.Module1.async;
import static edu.rice.hj.Module1.finish;
import static edu.rice.hj.Module1.launchHabaneroApp;
import static edu.rice.hj.Module2.isolated;

import java.util.Random;

import edu.rice.hj.api.HjSuspendable;
import edu.rice.hj.api.HjRunnable;
import edu.rice.hj.api.SuspendableException;

public class demo_example1 {

    public static int a = 1;

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
                                Random r = new Random();
                                if (r.nextInt() != 0) {
                                    isolated(new HjRunnable() {
                                        public void run() {
                                            a = 2;
                                            System.out.println("a = " + a);
                                        }
                                    });
                                } else {
                                    a = 2;
                                }
                                System.out.println("Hello World - 1");
                            }
                        });

                        async(new HjRunnable() {
                            @Override
                            public void run() {
                                isolated(new HjRunnable() {
                                    public void run() {
                                        a = 5;
                                    }
                                });
                                System.out.println("Hello World - 2");
                            }
                        });

                    }
                });
                System.out.println("value of a = " + a);
            }
        });
    }
}
