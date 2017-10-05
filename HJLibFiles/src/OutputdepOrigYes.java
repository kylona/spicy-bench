/*
A translation of: outputdep-orig-yes.c
Originally produced at the Lawrence Livermore National Laboratory
Written by Chunhua Liao, Pei-Hung Lin, Joshua Asplund,
Markus Schordan, and Ian Karlin

Translated at Brigham Young University by Lincoln Bergeson

*/

import static edu.rice.hj.Module1.forAll;
import static edu.rice.hj.Module2.launchHabaneroApp;
import edu.rice.hj.api.HjSuspendable;
import edu.rice.hj.api.SuspendableException;
import edu.rice.hj.api.HjSuspendingProcedure;

public class OutputdepOrigYes {

    static int x = 10;

    public static void main(String[] args) throws SuspendableException {

        int len = 100;
        int[] a = new int[len];

        launchHabaneroApp(new HjSuspendable() {
            public void run() throws SuspendableException {

                forAll(0,len-1,new HjSuspendingProcedure<Integer>() {
                    public void apply(Integer i) {
                        a[i] = x;
                        x = i;
                    }
                });
                System.out.println("x=" + x);

            }
        });
    }
}
