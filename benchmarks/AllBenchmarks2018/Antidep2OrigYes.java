import static permission.PermissionChecks.*;
/*
A translation of: antidep2-orig-yes.c
Originally produced at the Lawrence Livermore National Laboratory
Written by Chunhua Liao, Pei-Hung Lin, Joshua Asplund,
Markus Schordan, and Ian Karlin

Translated at Brigham Young University by Kyle Storey

*/
import static edu.rice.hj.Module2.launchHabaneroApp;
import static edu.rice.hj.Module2.forAll;
import edu.rice.hj.api.*;

public class Antidep2OrigYes {
  static int i, j;
  static int len = 20;
  static double[][] a = new double[len][len];
  public static void main(String[] args) throws SuspendableException {
      launchHabaneroApp(new HjSuspendable() {

          @Override
          public void run() throws SuspendableException {
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
            System.out.println("a[10][10]=" + a[10][10]);

          }

    });
  }
}
