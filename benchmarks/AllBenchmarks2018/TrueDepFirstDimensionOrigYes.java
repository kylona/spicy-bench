import static permission.PermissionChecks.*;
 
 
 
/*
A translation of: truedep1-var-yes.c
Originally produced at the Lawrence Livermore National Laboratory
Written by Chunhua Liao, Pei-Hung Lin, Joshua Asplund,
Markus Schordan, and Ian Karlin

Translated at Brigham Young University by Joshua Hooker

*/
import static edu.rice.hj.Module2.launchHabaneroApp;
import static edu.rice.hj.Module2.forAll;
import edu.rice.hj.api.*;

public class TrueDepFirstDimensionOrigYes {
  static int n = 1000, m = 1000;
  static double[][] b;
  public static void main(String[] args) throws SuspendableException {
    
    b = new double[n][m];
    for(int i = 0; i < n; i++)
        for (int j = 0; j < m; j++)
            b[i][j] = 0.5;
    
      launchHabaneroApp(new HjSuspendable() {

          @Override
          public void run() throws SuspendableException {

            forAll(1, n-1, new HjSuspendingProcedure<Integer>() {
              public void apply(Integer i) throws SuspendableException {
                acquireR(m);
                for(int j = 1; j < m; j++) {
                  acquireR(b[i-1], j-1);
                  acquireW(b[i], j);
                  b[i][j] = b[i-1][j-1];
                  releaseW(b[i], j);
                  releaseR(b[i-1], j-1);
                }
                releaseR(m);
              }
            });
            
          }
    });
    System.out.printf("b[500][500]=%f\n", b[500][500]);  
  }

}

