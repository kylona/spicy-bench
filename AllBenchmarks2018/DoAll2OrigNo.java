/*
A translation of: doall2-orig-no.c
Originally produced at the Lawrence Livermore National Laboratory
Written by Chunhua Liao, Pei-Hung Lin, Joshua Asplund,
Markus Schordan, and Ian Karlin

Translated at Brigham Young University by Kyle Storey

*/

import static edu.rice.hj.Module2.launchHabaneroApp;
import static edu.rice.hj.Module2.forAll;
import edu.rice.hj.api.*;

public class DoAll2OrigNo {
  static int[][] a = new int[100][100];
  public static void main(String[] args) throws SuspendableException {
      launchHabaneroApp(new HjSuspendable() {

          @Override
          public void run() throws SuspendableException {

            forAll(0, 100-1,new HjSuspendingProcedure<Integer>() {
              public void apply(Integer i) throws SuspendableException {
                for (int j = 0; j < 100; j++) {
                  a[i][j] = a[i][j]+1;
                }
              }
            });
          }

    });
  }
}
