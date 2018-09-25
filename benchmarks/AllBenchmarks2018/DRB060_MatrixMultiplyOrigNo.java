import static edu.rice.hj.Module2.launchHabaneroApp;
import static edu.rice.hj.Module2.forAll;
import edu.rice.hj.api.*;

public class DRB060_MatrixMultiplyOrigNo {

  static int N = 70;
  static int M = 70;
  static int K = 70;

  public static void main(String[] args) throws SuspendableException {
    double[][] a = new double[N][M];
    double[][] b = new double[M][K];
    double[][] c = new double[N][K];
    launchHabaneroApp(new HjSuspendable() {
      public void run() throws SuspendableException {
        forAll(0, N-1, new HjSuspendingProcedure<Integer>() {
          public void apply(Integer i) throws SuspendableException {
            for (int k = 0; k < K; k++) {
              for (int j = 0; j < M; j++) {
                c[i][j] = c[i][j] + a[i][k] * b[k][j];
              }
            }
          }
        });
      }
    });
  }

}
