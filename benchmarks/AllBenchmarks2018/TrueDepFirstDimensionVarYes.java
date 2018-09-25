 
 
 
 
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

public class TrueDepFirstDimensionVarYes {
  static int i,j;
  static int len = 1000;
  static int n,m;
  static double[][] b;
  public static void main(String[] args) throws SuspendableException {
    
    if (args.length > 1) len = Integer.parseInt(args[1]);
    n = len;
    m = len;
    b = new double[len][len];
    for(i = 0; i < n; i++)
        for (j = 0; j < m; j++)
            b[i][j] = 0.5;
    
      launchHabaneroApp(new HjSuspendable() {

          @Override
          public void run() throws SuspendableException {

            forAll(1, len-1, new HjSuspendingProcedure<Integer>() {
              public void apply(Integer i) throws SuspendableException {
                for(j = 1; j < m; j++)
                    b[i][j] = b[i-1][j-1];
              }
            });
            
          }
    });
  }

}

