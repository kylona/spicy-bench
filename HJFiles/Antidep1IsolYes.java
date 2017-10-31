/*
A variation of: antidep1-orig-yes.c
Originally produced at the Lawrence Livermore National Laboratory
Written by Chunhua Liao, Pei-Hung Lin, Joshua Asplund,
Markus Schordan, and Ian Karlin

Translated at Brigham Young University by Kyle Storey

*/
import static edu.rice.hj.Module2.*;
import static edu.rice.hj.Module0.launchHabaneroApp;
import static edu.rice.hj.Module1.async;
import static edu.rice.hj.Module2.isolated;
import edu.rice.hj.api.*;


public class Antidep1IsolYes {
  static int i;
  static int len = 25;
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
                isolated(new HjRunnable() {
                  public void run() {
                    a[i] = a[i+1] + 1;
                  }
                });
              }
            });

            System.out.println("a[5]=" + a[5]);

          }

    });
  }

}
