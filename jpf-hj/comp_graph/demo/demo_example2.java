package demo;

/*
 * Basic Example - forall
 * With Permission annotations
 */
import static edu.rice.hj.Module1.launchHabaneroApp;
import edu.rice.hj.api.HjSuspendable;
import edu.rice.hj.api.SuspendableException;
import static edu.rice.hj.Module1.forAll;
import edu.rice.hj.api.HjSuspendingProcedure;
import static permission.PermissionChecks.*;

public class demo_example2 {

    public static int[] a = {1};

    public static void main(final String[] args) {

        launchHabaneroApp(new HjSuspendable() {
            @Override
            public void run() throws SuspendableException {

                forAll(2, 3, new HjSuspendingProcedure<Integer>() {
                    @Override
                    public void apply(Integer arg) {
                        acquireW(a, 0, 0);
                        a[0] = 3;
                        releaseW(a, 0, 0);
                        acquireR(a, 0, 0);
                        System.out.println("Value of a = " + a[0]);
                        releaseR(a, 0, 0);
                    }
                });
            }
        });
    }
}
