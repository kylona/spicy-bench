import static edu.rice.hj.Module2.launchHabaneroApp;
import static edu.rice.hj.Module2.async;
import static edu.rice.hj.Module2.finish;
import edu.rice.hj.api.*;

public class SimpleSimpleSimple {
  public static void main(String[] args) throws SuspendableException {
      launchHabaneroApp(new HjSuspendable() {

         Integer i = 1;
         
	 @Override
          public void run() throws SuspendableException {
	    finish ( new HjSuspendable() {
              public void run() {
                async(new HjRunnable() {
                  public void run() {
                    i = i + 1;;
                  }
                });
                async(new HjRunnable() {
                  public void run() {
                    i = i * 2;
                  }
                });
              }
            });
            System.out.println("i is " + i);

        }

    });
  }


}
