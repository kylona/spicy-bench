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
  static int i;
  static int len = 20;
  static double[][] a;
  public static void main(String[] args) throws SuspendableException {
      launchHabaneroApp(new HjSuspendable() {

          @Override
          public void run() throws SuspendableException {

            if (args.length > 0)
                len = Integer.parseInt(args[0]);

            a = new double[len][len];

            for (i = 0; i < len; i++) {
              for (j = 0; j < len; j++) {
                a[i][j] = 0.5;
              }
            }

            forAll(0, len-2, new HjSuspendingProcedure<Integer>() {
              public void apply(Integer i) throws SuspendableException {
                for (int j = 0; j < len; j += 1) {
                  a[i][j] += a[i+1][j];
                }
              }
            });
          }

    });
  }
}
