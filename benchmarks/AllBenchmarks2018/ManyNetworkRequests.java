import static edu.rice.hj.Module2.launchHabaneroApp;
import static edu.rice.hj.Module2.forAll;
import edu.rice.hj.api.*;
import static permission.PermissionChecks.*;

/**
 * Simulate use case where we need to hit the same endpoint
 * with many different arguments so we launch a lightweight
 * task for each network request.
 */
public class ManyNetworkRequests {

  static int N = 1000;

  public static void main(String[] args) throws SuspendableException {
    //int[] params = new int[N];
    int[] results = new int[N];
    launchHabaneroApp(new HjSuspendable() {
      public void run() throws SuspendableException {
        forAll(0, N-1, new HjSuspendingProcedure<Integer>() {
          public void apply(Integer i) throws SuspendableException {
            acquireW(results, i);
            results[i] = networkRequest(i);//params[i]);
            releaseW(results, i);
          }
        });
      }
    });
  }

  public static int networkRequest(int param) {
    // Just simulate request
    return param;
  }

}
