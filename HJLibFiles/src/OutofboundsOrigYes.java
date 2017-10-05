/*
A translation of: outofbounds-orig-yes.c
Originally produced at the Lawrence Livermore National Laboratory
Written by Chunhua Liao, Pei-Hung Lin, Joshua Asplund,
Markus Schordan, and Ian Karlin

Translated at Brigham Young University by Lincoln Bergeson

*/

/* The outmost loop is parallelized.
   But the inner level loop has out of bound access for b[i][j]
   when j==0. 
   This will case memory access of a previous row's last element.
  For example, an array of 4x4: 
      j=0 1 2 3
   i=0  x x x x
     1  x x x x
     2  x x x x
     3  x x x x
     
    outer loop: i=2, 
    inner loop: j=0
    array element accessed b[i][j-1] becomes b[2][-1], which in turn is b[1][3]
    due to linearized row-major storage of the 2-D array.
    This causes loop-carried data dependence between i=2 and i=1.
*/

import static edu.rice.hj.Module1.forAll;
import static edu.rice.hj.Module2.launchHabaneroApp;
import edu.rice.hj.api.HjSuspendable;
import edu.rice.hj.api.SuspendableException;
import edu.rice.hj.api.HjSuspendingProcedure;

public class OutofboundsOrigYes {

    public static void main(String[] args) throws SuspendableException {

        int n = 100;
        int m = 100;
        double b[][] = new double[n][m];

        launchHabaneroApp(new HjSuspendable() {
            public void run() throws SuspendableException {

                forAll(0,n-1,new HjSuspendingProcedure<Integer>() {
                    public void apply(Integer i) throws SuspendableException {
                        for (int j = 0; j < m; j++) {
                            b[i][j] = b[i][j-1]; // note that this throws ArrayIndexOutOfBoundsException
                        }
                    }
                });

            }
        });

        System.out.println("b[50][50]=" + b[50][50]);
    }
}

