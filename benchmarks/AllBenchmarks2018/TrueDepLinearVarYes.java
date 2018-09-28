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

public class TrueDepLinearVarYes {
  static int i;
  static int len = 2000;
  static int[] a;
  public static void main(String[] args) throws SuspendableException {
    
    if (args.length > 1) len = Integer.parseInt(args[1]);
    a = new int[len];
    
    for(i = 0; i < len; i++)
        a[i] = i;
    
      launchHabaneroApp(new HjSuspendable() {

          @Override
          public void run() throws SuspendableException {

            forAll(0, len/2, new HjSuspendingProcedure<Integer>() {
              public void apply(Integer i) throws SuspendableException {
                acquireR(a, i);
                acquireW(a, 2 * i + 1);
                a[2*i+1] = a[i] + 1;
                releaseW(a, 2 * i + 1);
                releaseR(a, i);
              }
            });
            
          }
    });
  }

}

