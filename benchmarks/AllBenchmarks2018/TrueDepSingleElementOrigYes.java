import static permission.PermissionChecks.*;

/*
A translation of: truedep1-var-yes.c
Originally produced at the Lawrence Livermore National Laboratory
Written by Chunhua Liao, Pei-Hung Lin, Joshua Asplund,
Markus Schordan, and Ian Karlin

Translated at Brigham Young University by Joshua Hooker

*/
import static edu.rice.hj.Module2.launchHabaneroApp;
import static edu.rice.hj.Module2.forAll;
import edu.rice.hj.api.*;

public class TrueDepSingleElementOrigYes {
  static int i;
  static int len = 1000;
  static int[] a;
  public static void main(String[] args) throws SuspendableException {
    
    a = new int[len];
    a[0] = 2;
    
      launchHabaneroApp(new HjSuspendable() {

          @Override
          public void run() throws SuspendableException {

            forAll(0, len-1, new HjSuspendingProcedure<Integer>() {
              public void apply(Integer i) throws SuspendableException {
                acquireR(a, 0);
                acquireW(a, i);
                a[i] = a[i] + a[0];
                releaseW(a, i);
                releaseR(a, 0);
              }
            });
            System.out.println("a[500]=" + a[500]);
            
          }
    });
  }

}
