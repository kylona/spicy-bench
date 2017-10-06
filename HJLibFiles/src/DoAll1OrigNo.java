/*
A translation of: doall1-orig-no.c
Originally produced at the Lawrence Livermore National Laboratory
Written by Chunhua Liao, Pei-Hung Lin, Joshua Asplund,
Markus Schordan, and Ian Karlin

Translated at Brigham Young University by Kyle Storey

*/

import static edu.rice.hj.Module2.launchHabaneroApp;
import static edu.rice.hj.Module2.forAll;
import edu.rice.hj.api.*;

public class DoAll1OrigNo {
  static int[] a = new int[100];
  public static void main(String[] args) throws SuspendableException {
      launchHabaneroApp(new HjSuspendable() {

          @Override
          public void run() throws SuspendableException {

            forAll(0, 100-1, new HjProcedure<Integer>() {
              public void apply(Integer i) {
                a[i] = a[i]+1;
              }
            });
          }

    });
  }
}
