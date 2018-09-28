import static permission.PermissionChecks.*;

import static edu.rice.hj.Module0.finish;
import static edu.rice.hj.Module0.launchHabaneroApp;
import static edu.rice.hj.Module1.async;
import static edu.rice.hj.Module2.isolated;

import edu.rice.hj.api.HjRunnable;
import edu.rice.hj.api.HjSuspendable;

public class DataRaceIsolateSimple {

    private static int g = 0;

    public static void main(final String[] args) {
        launchHabaneroApp(new HjSuspendable() {
        	public void run() {
                finish(new HjSuspendable() {
                	public void run() {
                        acquireW(DataRaceIsolateSimple.g);
                    	DataRaceIsolateSimple.g = 0;
                        releaseW(DataRaceIsolateSimple.g);
                    	async(new HjRunnable() {
                                public void run() {
                                	p();
                                }
                    	});	
                    	isolated(new HjRunnable() {
                    		public void run() {
                    			System.out.println("Isolated: m");
                                acquireW(DataRaceIsolateSimple.g);
                    			DataRaceIsolateSimple.g = 1;
                                releaseW(DataRaceIsolateSimple.g);
                    		}
                    	});
                	}
                });
                
        	}
        });
    }

    private static void p() {
    	isolated(new HjRunnable() {
    		public void run() {
    			System.out.println("Isolated: p");
    		}	
    	});	
        acquireW(DataRaceIsolateSimple.g);
    	DataRaceIsolateSimple.g = 2;
        releaseW(DataRaceIsolateSimple.g);
    }
  
}
