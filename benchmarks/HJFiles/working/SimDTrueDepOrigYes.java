 
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

public class TrueDep1VarYes {
  static int i;
  static int len = 100;
  static int a[100], b[100];
  public static void main(String[] args) throws SuspendableException {
    
    for(i=0;i<len;i++)
    {
        a[i]=i;
        b[i]=i+1;
    }
    
      launchHabaneroApp(new HjSuspendable() {

          @Override
          public void run() throws SuspendableException {

            forAll(0, len-1, new HjSuspendingProcedure<Integer>() {
              public void apply(Integer i) throws SuspendableException {
                a[i] = a[i] + a[0];
              }
            });
          }
    });
    for(i = 0; i < len; i++)
        printf("i=%d a[%d]=%d\n", i,i,a[i]);
  }

}
 
