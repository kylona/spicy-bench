/*
A translation of: antidep1-orig-yes.c
Originally produced at the Lawrence Livermore National Laboratory
Written by Chunhua Liao, Pei-Hung Lin, Joshua Asplund,
Markus Schordan, and Ian Karlin

Translated at Brigham Young University by Kyle Storey

*/
import static edu.rice.hj.Module2.launchHabaneroApp;
import static edu.rice.hj.Module2.forAll;
import edu.rice.hj.api.*;

public class Antidep1OrigYes {
  static int i;
  static int len = 1000;
  static int[] a = new int[len];
  public static void main(String[] args) throws SuspendableException {
      launchHabaneroApp(new HjSuspendable() {

          @Override
          public void run() throws SuspendableException {

            for (i = 0; i < len; i++) {
                a[i] = i;
            }

            forAll(0, len-2, new HjSuspendingProcedure<Integer>() {
              public void apply(Integer i) throws SuspendableException {
                acquireW(a, i);
                acquireR(a, i+1);
                a[i] = a[i+1] + 1;
                releaseR(a, i+1);
                releaseW(a, i);
              }
            });

            System.out.println("a[500]=" + a[500]);

          }

    });
  }

}
