import static permission.PermissionChecks.*;
/*
A translation of: doall1-orig-no.c
Originally produced at the Lawrence Livermore National Laboratory
Written by Chunhua Liao, Pei-Hung Lin, Joshua Asplund,
Markus Schordan, and Ian Karlin

Translated at Brigham Young University by Kyle Storey

*/

import edu.rice.hj.api.HjSuspendable;
import edu.rice.hj.api.HjSuspendingProcedure;
import edu.rice.hj.api.SuspendableException;

import static edu.rice.hj.Module2.forAll;
import static edu.rice.hj.Module2.launchHabaneroApp;

public class DRB041_3mmParallelNo {
  static int[] a = new int[100];
  static int[] b = new int[100];
  static int[] c = new int[100];

  public static void main(String[] args) throws SuspendableException {
    launchHabaneroApp(new HjSuspendable() {
      @Override
      public void run() throws SuspendableException {
        forAll(0, 100 - 1, new HjSuspendingProcedure<Integer>() {
          public void apply(Integer i) throws SuspendableException {
            acquireR(b, i);
            acquireR(c, i);
            acquireW(a, i);
            a[i] = b[i]*c[i];
            releaseW(a, i);
            releaseR(c, i);
            releaseR(b, i);
          }
        });
        for(int i = 0; i < 100; i++) {
          System.out.println(a[i] + ", ");
        }
      }
    });
  }
}
