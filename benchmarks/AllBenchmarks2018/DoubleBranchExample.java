import static permission.PermissionChecks.*;

import static edu.rice.hj.Module0.finish;
import static edu.rice.hj.Module0.launchHabaneroApp;
import static edu.rice.hj.Module1.async;
import static edu.rice.hj.Module2.isolated;

import edu.rice.hj.api.HjRunnable;
import edu.rice.hj.api.HjSuspendable;

public class DoubleBranchExample {

    public static void main(final String[] args) {
        launchHabaneroApp(new HjSuspendable() {
        	public void run() {
                finish(new HjSuspendable() {
                	public void run() {
                    	async(new HjRunnable() {
                            public void run() {
                                p();    
                            }
                    	});	
                        p();
                	}
                });
                
        	}
        });
    }

    private static void p() {
        async(new HjRunnable() {
            public void run() {
                System.out.println("Did Stuff");
            }
        });
        System.out.println("Did Stuff");
    }
  
}
