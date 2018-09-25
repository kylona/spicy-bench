
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

public class TrueDepSingleElementVarYes {
  static int i;
  static int len = 1000;
  static int[] a;
  public static void main(String[] args) throws SuspendableException {
    
    if (args.length > 1) len = Integer.parseInt(args[1]);
    
    a = new int[len];
    a[0] = 2;
    
      launchHabaneroApp(new HjSuspendable() {

          @Override
          public void run() throws SuspendableException {

            forAll(0, len-1, new HjSuspendingProcedure<Integer>() {
              public void apply(Integer i) throws SuspendableException {
                a[i] = a[i] + a[0];
              }
            });
          }
    });
  }

}
