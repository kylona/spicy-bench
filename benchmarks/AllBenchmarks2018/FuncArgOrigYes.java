import static permission.PermissionChecks.*;
 
/*
A translation of: func-arg-orig-yes.c
Originally produced at the Lawrence Livermore National Laboratory
Written by Chunhua Liao, Pei-Hung Lin, Joshua Asplund,
Markus Schordan, and Ian Karlin

Translated at Brigham Young University by Joshua Hooker

*/
import static edu.rice.hj.Module2.launchHabaneroApp;
import static edu.rice.hj.Module2.forAll;
import edu.rice.hj.api.*;

public class FuncArgOrigYes {
  static int i;

  public static void f1(int q)
  {
      acquireW(q);
      q += 1;
      releaseW(q);
  }
  public static void main(String[] args) throws SuspendableException {

      launchHabaneroApp(new HjSuspendable() {

          @Override
          public void run() throws SuspendableException {

            forAll(0, 4, new HjSuspendingProcedure<Integer>() {
              public void apply(Integer i) throws SuspendableException {
                f1(i);
              }
            });
          }
    });
    System.out.printf("i=%d\n",i);
  }

}
