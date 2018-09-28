import static permission.PermissionChecks.*;
 
/*
A translation of: minusminus--orig-yes.c
Originally produced at the Lawrence Livermore National Laboratory
Written by Chunhua Liao, Pei-Hung Lin, Joshua Asplund,
Markus Schordan, and Ian Karlin

Translated at Brigham Young University by Joshua Hooker

*/
import static edu.rice.hj.Module2.launchHabaneroApp;
import static edu.rice.hj.Module2.forAll;
import edu.rice.hj.api.*;

public class MinusMinusOrigYes {
  static int i;
  static int len = 100;
  static int numNodes;
  static int numNodes2;
  static int[] x;
  public static void main(String[] args) throws SuspendableException {

    numNodes = len;
    numNodes2 = 0;
    x = new int[100];
    
      // initialize x[]
    for (i=0; i< len; i++)
    {
        if (i%2==0)
            x[i]=5;
        else
            x[i]= -5;
    }

      launchHabaneroApp(new HjSuspendable() {

          @Override
          public void run() throws SuspendableException {

            forAll(0, numNodes-1, new HjSuspendingProcedure<Integer>() {
              public void apply(Integer i) throws SuspendableException {
                acquireR(x);
                if(x[i]<=0) {
                    acquireW(numNodes2);
                    numNodes2--;
                    releaseW(numNodes2);
                }
                releaseR(x);
              }
            });
          }
    });
    System.out.printf("numNodes2 = %d\n", numNodes2);
  }

}
