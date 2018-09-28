import static permission.PermissionChecks.*;
 /*
A translation of: antidep1-orig-yes.c
Originally produced at the Lawrence Livermore National Laboratory
Written by Chunhua Liao, Pei-Hung Lin, Joshua Asplund,
Markus Schordan, and Ian Karlin

Translated at Brigham Young University by Joshua Hooker

*/
import static edu.rice.hj.Module2.launchHabaneroApp;
import static edu.rice.hj.Module2.forAll;
import edu.rice.hj.api.*;

public class LinearMissingOrigYes {
  static int i,j = 0;
  static int len = 100;
  static double[] a,b,c;
  public static void main(String[] args) throws SuspendableException {
    a = new double[len];
    b = new double[len];
    c = new double[len];

    for (i=0;i<len;i++)
    {
      a[i]=((double)i)/2.0; 
      b[i]=((double)i)/3.0; 
      c[i]=((double)i)/7.0; 
    }
      launchHabaneroApp(new HjSuspendable() {

          @Override
          public void run() throws SuspendableException {

            forAll(0, len-1, new HjSuspendingProcedure<Integer>() {
              public void apply(Integer i) throws SuspendableException {
                acquireW(c, j);
                acquireR(a, i);
                acquireR(b, i);
                c[j] += a[i] * b[i];
                releaseR(b, i);
                releaseR(a, i);
                releaseW(c, j);

                acquireW(j);
                acquireR(j);
                j++;
                releaseR(j);
                releaseW(j);
              }
            });

            System.out.println("c[50]=" + c[50]);
          }

    });
  }

}
