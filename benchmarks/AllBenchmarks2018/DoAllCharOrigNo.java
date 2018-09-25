/*
A translation of: doallchar-orig-no.c
Originally produced at the Lawrence Livermore National Laboratory
Written by Chunhua Liao, Pei-Hung Lin, Joshua Asplund,
Markus Schordan, and Ian Karlin

Translated at Brigham Young University by Benjamin Ogles
*/


import static edu.rice.hj.Module2.launchHabaneroApp;
import static edu.rice.hj.Module2.forAll;
import edu.rice.hj.api.*;

// one dimension array computation
// with finer granularity than traditional 4 bytes.
// Dynamic tools looking at 4-bytes elements may wrongfuly report race condition.

public class DoAllCharOrigNo {
  static char[] a = new char[100];
  public static void main(String[] args) throws SuspendableException {
    launchHabaneroApp(new HjSuspendable() {
      @Override
      public void run() throws SuspendableException {
        forAll(0, 100-1, new HjSuspendingProcedure<Integer>() {
          public void apply(Integer i) throws SuspendableException {
            a[i] = a[i] + 1;
          }
        });
      }
    });
  }
}
