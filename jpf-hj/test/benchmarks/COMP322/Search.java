package benchmarks.COMP322;

import edu.rice.hj.api.SuspendableException;

import static edu.rice.hj.Module1.*;
import edu.rice.hj.api.HjRunnable;
import edu.rice.hj.api.HjSuspendable;
import static permission.PermissionChecks.*;

/**
 * Reads in two strings, the pattern and the input text, and searches for the
 * pattern in the input text.
 * <p>
 * % hj Search rabrabracad abacadabrabracabracadabrabrabracad text:
 * abacadabrabracabracadababacadabrabracabracadabrabrabracad pattern:
 * rabrabracad
 * <p>
 * HJ version ported from Java version in
 * http://algs4.cs.princeton.edu/53substring/Brute.java.html
 *
 * @author Vivek Sarkar (vsarkar@rice.edu)
 */
public class Search {

    private static final String default_pat = "rabrabracad";
    private static final String default_txt = "rabrabracadabacadabrabracabracadababacadabrabracabracadabrabrabracad";

    // return number of occurrences of pattern in text
    /**
     * <p>
     * main.</p>
     *
     * @param args an array of {@link String} objects.
     */
    public static void main(final String[] args) {

        final String pat = args.length >= 1 ? args[0] : default_pat;
        final String txt = args.length >= 2 ? args[1] : default_txt;

        final char[] pattern = pat.toCharArray();
        final char[] text = txt.toCharArray();

        launchHabaneroApp(() -> {
            finish(() -> {
                boolean parFound;
                try {
                    parFound = searchPar(pattern, text);
                    System.out.println("Pattern found by parallel algorithm: " + parFound);
                } catch (SuspendableException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            });
        });
    }

    //Write/write barrier read?
    //How does JPF handle static initialization?
    // test client
    /**
     * <p>
     * searchPar.</p>
     *
     * @param pattern an array of char.
     * @param text an array of char.
     * @return a boolean.
     */
    public static boolean searchPar(final char[] pattern, final char[] text) throws SuspendableException {
        final int M = pattern.length;
        final int N = text.length;
        final boolean[] found = {false};
        finish(new HjSuspendable() {
            public void run() {
                for (int i = 0; i <= N - M; i++) {
                    final int ii = i;
                    async(new HjRunnable() {
                        public void run() {
                            int j;
                            for (j = 0; j < M; j++) {
                                if (text[ii + j] != pattern[j]) {
                                    break;
                                }
                            }
                            if (j == M) {
                                acquireW(found, 0);
                                found[0] = true;
                                releaseW(found, 0);
                            }
                        }
                    });
                }
            }
        });
        acquireR(found, 0);
        boolean retVal = found[0];
        releaseR(found, 0);
        return retVal;

    }
}
