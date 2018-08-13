import static edu.rice.hj.Module2.launchHabaneroApp;
import edu.rice.hj.api.HjSuspendable;
import edu.rice.hj.api.SuspendableException;


public class Template {

    public static void main(String[] args) throws SuspendableException {

        // set up, etc.

        launchHabaneroApp(new HjSuspendable() {
            public void run() throws SuspendableException {

                // run benchmarks

            }
        });

        // print out results if applicable
    }
}
