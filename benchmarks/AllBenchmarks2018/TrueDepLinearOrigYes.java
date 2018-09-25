 
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

public class TrueDepLinearOrigYes {
  static int i;
  static int[] a;
  public static void main(String[] args) throws SuspendableException {
    
    a = new int[2000];
    
    for(i = 0; i < 2000; i++)
        a[i] = i;
    
      launchHabaneroApp(new HjSuspendable() {

          @Override
          public void run() throws SuspendableException {

            forAll(0, 999, new HjSuspendingProcedure<Integer>() {
              public void apply(Integer i) throws SuspendableException {
                a[2*i+1] = a[i] + 1;
              }
            });
            System.out.println("a[1001]=" + a[1001]);
            
          }
    });
  }

}

