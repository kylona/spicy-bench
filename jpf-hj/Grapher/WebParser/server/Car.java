import static edu.rice.hj.Module0.finish;
import static edu.rice.hj.Module0.launchHabaneroApp;
import static edu.rice.hj.Module1.async;
import edu.rice.hj.api.HjRunnable;
import edu.rice.hj.api.HjSuspendable;
import edu.rice.hj.api.SuspendableException;
import java.util.Random;

public class Car {

    public static final int LIMIT = 100;

    public static void main(final String[] args) {
        //General initialization
        launchHabaneroApp(new HjSuspendable() {
            @Override
            public void run() throws SuspendableException {
                //First, complete construction phase one.
                finish(new HjSuspendable() {
                    @Override
                    public void run() {
                        assembleFrame();
                        buildEngine();
                        constructElectronics();
                    }
                });
                //Now move on to phase two.
                finish(new HjSuspendable() {
                    @Override
                    public void run() {
                        attatchEngine();
                        assembleBodyOnFrame();
                        ComponentsFactory.installWindshield();
                    }
                });
                //Painting has to be done sequentially.
                finish(new HjSuspendable() {
                    @Override
                    public void run() {
                        ComponentsFactory.paintCar();
                    }
                });

                //Lastly, place the tires on the car and roll it out of the factory.
                for (int i = 0; i < 4; i++) {
                    makeTire();
                }
            }
        });
    }

    private static void assembleFrame() {
        async(new HjRunnable() {
            @Override
            public void run() {
                doWork();
                System.out.println("Body is assembled, boop boop.");
            }
        });
    }

    private static void buildEngine() {
        async(new HjRunnable() {
            @Override
            public void run() {
                doWork();
                System.out.println("Engine is assembled, boop boop.");
            }
        });
    }

    private static void constructElectronics() {
        async(new HjRunnable() {
            @Override
            public void run() {
                doWork();
                System.out.println("Electronics are assembled, boop boop.");
            }
        });
    }

    private static void makeTire() {
        async(new HjRunnable() {
            @Override
            public void run() {
                doWork();
                System.out.println("Tire is made, beep boop.");
            }

        });
    }

    public static void doWork() {
        Random random = new Random();
        int loops = random.nextInt(LIMIT);
        for (int i = 0; i < loops; i++) {
            for (int j = 0; j < loops; j++) {
                double temp = random.nextDouble();
                temp = Math.pow(temp, random.nextInt(10));
                temp = temp * 2 % LIMIT;
                //System.out.print(("" + temp).substring(0, 1));
            }
        }

    }

    private static void attatchEngine() {
        doWork();
        System.out.println("Engine is placed, boop boop.");
    }

    private static void assembleBodyOnFrame() {
        doWork();
        System.out.println("Body is placed, boop boop.");
    }


}