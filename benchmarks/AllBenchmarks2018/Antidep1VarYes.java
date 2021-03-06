import static permission.PermissionChecks.*;
/*
A translation of: antidep1-var-yes.c
Originally produced at the Lawrence Livermore National Laboratory
Written by Chunhua Liao, Pei-Hung Lin, Joshua Asplund,
Markus Schordan, and Ian Karlin

Translated at Brigham Young University by Kyle Storey

*/
import static edu.rice.hj.Module2.launchHabaneroApp;
import static edu.rice.hj.Module2.forAll;
import edu.rice.hj.api.*;


public class Antidep1VarYes {
  static int i, j;
  static int len = 20;
  static double[][] a;

  public static void main(String[] args) throws SuspendableException {
    launchHabaneroApp(new HjSuspendable() {

        @Override
        public void run() throws SuspendableException {

          if (args.length > 0) {
            len = Integer.parseInt(args[0]);
          }

          a = new double[len][len];

          for (i = 0; i < len; i++) {
            for (j = 0; j < len; j++) {
              a[i][j] = 0.5;
            }
          }


          forAll(0, len-2, new HjSuspendingProcedure<Integer>() {
            public void apply(Integer i) throws SuspendableException {
              acquireW(a[i], 0, len-1);
              acquireR(a[i+1], 0, len-1);
              for (j = 0; j < len; j += 1) {
                a[i][j] += a[i+1][j];
              }
              releaseR(a[i], 0, len-1);
              releaseW(a[i+1], 0, len-1);
            }
          });
        }
    });
  }
}
