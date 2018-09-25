/*
A translation of: DRB002-antidep1-var-yes.c
Originally produced at the Lawrence Livermore National Laboratory
Written by Chunhua Liao, Pei-Hung Lin, Joshua Asplund,
Markus Schordan, and Ian Karlin

Translated at Brigham Young University by Joseph Jones

*/
import static edu.rice.hj.Module2.launchHabaneroApp;
import static edu.rice.hj.Module2.forAll;
import edu.rice.hj.api.*;


public class DRB002_Antidep1VarYes {
  static int i;
  static int len = 1000;
  static int[] a;

  public static void main(String[] args) throws SuspendableException {
    launchHabaneroApp(new HjSuspendable() {

        @Override
        public void run() throws SuspendableException {

          if (args.length > 0) {
            len = Integer.parseInt(args[0]);
          }

          a = new int[len];

          for (i = 0; i < len; i++) {
            a[i] = i;
          }


          forAll(0, len-2, new HjSuspendingProcedure<Integer>() {
            public void apply(Integer i) throws SuspendableException {
              a[i] = a[i+1] + 1;
            }
          });
        }
    });
  }
}
