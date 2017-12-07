
import static edu.rice.hj.Module0.finish;
import static edu.rice.hj.Module0.launchHabaneroApp;
import static edu.rice.hj.Module1.async;
import static edu.rice.hj.Module2.isolated;

import edu.rice.hj.api.HjRunnable;
import edu.rice.hj.api.HjSuspendable;

public class DataRaceIsolateSimple1 {

    private static int shared = 0;

    public static void main(final String[] args) {
        launchHabaneroApp(new HjSuspendable() {
        	public void run() {
                finish(new HjSuspendable() {
                	public void run() {
                    	DataRaceIsolateSimple1.shared = 0;
                    	async(new HjRunnable() {
                                public void run() {
                                	isolated(new HjRunnable() {
                                		public void run() {
                                			System.out.println("Isolated: m");
                                			DataRaceIsolateSimple1.shared++;
                                		}
                                	});
                                }
                    	});	
                       	async(new HjRunnable() {
                            public void run() {
                            	p();
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
    			DataRaceIsolateSimple1.shared = 0;
    		}	
    	});	
    	DataRaceIsolateSimple1.shared = 0;
    }
  
}
