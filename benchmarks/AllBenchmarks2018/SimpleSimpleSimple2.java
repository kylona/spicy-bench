import static permission.PermissionChecks.*;
import static edu.rice.hj.Module2.launchHabaneroApp;
import static edu.rice.hj.Module2.async;
import static edu.rice.hj.Module2.finish;
import edu.rice.hj.api.*;

public class SimpleSimpleSimple2 {
  public static void main(String[] args) throws SuspendableException {
      launchHabaneroApp(new HjSuspendable() {

         Integer i = 1;
         
	 @Override
          public void run() throws SuspendableException {
	    finish ( new HjSuspendable() {
              public void run() {
                async(new HjRunnable() {
                  public void run() {
                    acquireW(i);
                    i = i + 1;;
                    releaseW(i);
                  }
                });
                async(new HjRunnable() {
                  public void run() {
                    acquireW(i);
                    i = i * 2;
                    releaseW(i);
                  }
                });
              }
            });
            System.out.println("I can check any code you want");
            System.out.println("i is " + i);

        }

    });
  }


}
