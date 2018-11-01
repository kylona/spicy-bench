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

public class TrueDepLinearOrigYes {
  static int i;
  static int[] a;
  public static void main(String[] args) throws SuspendableException {
    
    a = new int[200];
    
    for(i = 0; i < 200; i++)
        a[i] = i;
    
      launchHabaneroApp(new HjSuspendable() {

          @Override
          public void run() throws SuspendableException {

            forAll(0, 99, new HjSuspendingProcedure<Integer>() {
              public void apply(Integer i) throws SuspendableException {
                acquireR(a, i);
                acquireW(a, 2 * i + 1);
                a[2*i+1] = a[i] + 1;
                releaseW(a, 2 * i + 1);
                releaseR(a, i);
              }
            });
            System.out.println("a[1001]=" + a[101]);
            
          }
    });
  }

}

