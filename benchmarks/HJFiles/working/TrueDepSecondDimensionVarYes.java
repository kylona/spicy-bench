
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

public class TrueDepSecondDimensionVarYes {
  static int i,j;
  static int n,m;
  static int len = 1000;
  static double[][] b;
  public static void main(String[] args) throws SuspendableException {
    
    if(args.length > 1) len = Integer.parseInt(args[1]);
    
    b = new double[len][len];
    
      launchHabaneroApp(new HjSuspendable() {

          @Override
          public void run() throws SuspendableException {
            for(i = 0
            forAll(1, j<m, new HjSuspendingProcedure<Integer>() {
              public void apply(Integer i, Integer j) throws SuspendableException {
                    for (j=1;j<m;j++)
                        b[i][j]=b[i][j-1]
              }
            });
            
          }
    });
  }

}
 
