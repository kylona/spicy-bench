/*
A translation of: fprintf-org-no.c
Originally produced at the Lawrence Livermore National Laboratory
Written by Chunhua Liao, Pei-Hung Lin, Joshua Asplund,
Markus Schordan, and Ian Karlin

Translated at Brigham Young University by Kyle Storey

*/

//Is FileWriter.write thread safe?
//Even if it is thread save, is there a data race?

import static edu.rice.hj.Module2.launchHabaneroApp;
import static edu.rice.hj.Module2.forAll;
import edu.rice.hj.api.*;

import java.io.*;

public class fprintfOrigNo {
    static int i;
    static boolean ret;
    static FileWriter pfile = null;
    static int len=1000;
    static int[] A = new int[1000];

    public static void main(String[] args) throws SuspendableException {
        launchHabaneroApp(new HjSuspendable() {

            @Override
            public void run() throws SuspendableException {
              for (int i = 0; i < len; i++) {
                A[i] = i;
              }

              try {
                pfile = new FileWriter("mytempfile.txt");
              }
              catch (IOException ex) {
                System.err.println("Error opening File()\n");
              }

              forAll(0, len-1, new HjSuspendingProcedure<Integer>() {
                public void apply(Integer i) throws SuspendableException {
                  try {
                    pfile.write(Integer.toString(A[i]) + "\n");
                  } catch (IOException ignore) { }
                }
              });

              try {
                pfile.close();
              }
              catch (IOException ex) {
                System.err.println("Error closing File()\n");
              }

              ret = new File("mytempfile.txt").delete();
              if (!ret) {
                System.err.println("Error: unable to delete mytempfile.txt\n");
              }
            }

      });
    }

}
