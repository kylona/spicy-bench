
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
                    	DataRaceIsolateSimple.g = 0;
                    	async(new HjRunnable() {
                                public void run() {
                                	p();
                                }
                    	});	
                    	isolated(new HjRunnable() {
                    		public void run() {
                    			System.out.println("Isolated: m");
                    			DataRaceIsolateSimple.g = 1;
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
    	DataRaceIsolateSimple.g = 2;
    }
  
}
