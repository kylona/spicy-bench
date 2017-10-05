import static edu.rice.hj.Module1.forAll;
import static edu.rice.hj.Module2.launchHabaneroApp;
import edu.rice.hj.api.HjSuspendable;
import edu.rice.hj.api.SuspendableException;

public class Forall {

	int a[];
	
	public static void main(String[] args) throws SuspendableException {
		Forall f = new Forall();
		f.run();
        f.print();
	}

	public void run() throws SuspendableException {
		a = new int[100];
        launchHabaneroApp(new HjSuspendable() {
            public void run() throws SuspendableException {
                forAll(0, 98, (i) -> {
                    a[i] = a[i+1];
                });
            }
        });
	}

    public void print() {
        for (int i : a) {
            System.out.print(i + "\t");
        }
    }
}
