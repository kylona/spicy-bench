import static edu.rice.hj.Module2.launchHabaneroApp;
import static edu.rice.hj.Module2.forAll;
import static edu.rice.hj.Module2.isolated;
import edu.rice.hj.api.*;


public class IsolatedBlockNo {
  static int j = 0;

  public static void main(String[] args) throws SuspendableException {
      launchHabaneroApp(new HjSuspendable() {
        int numThreads = 2;
          @Override
          public void run() throws SuspendableException {
            forAll(0, numThreads, new HjSuspendingProcedure<Integer>() {
              public void apply(Integer i) throws SuspendableException {
                isolated(new HjRunnable() {
                  public void run() {
                    j = j + i;
                  }
                });

                //isolated(new HjRunnable() {
                //  public void run() {
                //    j = j + i;
                //  }
                //});

              }
            });

        }

    });

  }
  private static void doWork() {
      int loops = 100;
      for (int i = 0; i < loops; i++) {
          for (int j = 0; j < loops; j++) {
              double temp = 0.2564525;
              temp = Math.pow(temp, 10);
              temp = temp * 2 % 100;
          }
      }

  }

}
