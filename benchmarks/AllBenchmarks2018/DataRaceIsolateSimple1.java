
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
                        acquireW(DataRaceIsolateSimple1.shared);
                    	DataRaceIsolateSimple1.shared = 0;
                        releaseW(DataRaceIsolateSimple1.shared);
                    	async(new HjRunnable() {
                                public void run() {
                                	isolated(new HjRunnable() {
                                		public void run() {
                                			System.out.println("Isolated: m");
                                            acquireW(DataRaceIsolateSimple1.shared);
                                			DataRaceIsolateSimple1.shared++;
                                            releaseW(DataRaceIsolateSimple1.shared);
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
                acquireW(DataRaceIsolateSimple1.shared);
                DataRaceIsolateSimple1.shared = 0;
                releaseW(DataRaceIsolateSimple1.shared);
    		}	
    	});	
        acquireW(DataRaceIsolateSimple1.shared);
        DataRaceIsolateSimple1.shared = 0;
        releaseW(DataRaceIsolateSimple1.shared);
    }
  
}
