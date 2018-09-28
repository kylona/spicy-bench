import static permission.PermissionChecks.*;
/*
A translation of: antidep2-orig-yes.c
Originally produced at the Lawrence Livermore National Laboratory
Written by Chunhua Liao, Pei-Hung Lin, Joshua Asplund,
Markus Schordan, and Ian Karlin

Translated at Brigham Young University by Joseph Jones

*/
import static edu.rice.hj.Module2.launchHabaneroApp;
import static edu.rice.hj.Module2.forAll;
import edu.rice.hj.api.*;

public class DRB010_LastPrivateMissingVarYes {
  static int i, x;
  static int len = 10000;
  public static void main(String[] args) throws SuspendableException {
      launchHabaneroApp(new HjSuspendable() {

          @Override
          public void run() throws SuspendableException {
            if( args.length > 0)
                len = Integer.parseInt(args[0]);
            forAll(0, len-1, new HjSuspendingProcedure<Integer>() {
              public void apply(Integer i) throws SuspendableException {
                 acquireW(x);
                 x = i; 
                 releaseW(x);
              }
            });
            System.out.printf("x=%d", x);

          }

    });
  }
}
