/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lib.runtime.async;

import static edu.rice.hj.Module0.launchHabaneroApp;
import static edu.rice.hj.Module1.async;
import static edu.rice.hj.Module2.isolated;
import edu.rice.hj.api.HjRunnable;
import edu.rice.hj.api.HjSuspendable;

/**
 *
 * @author kristophermiles
 */
public class AsyncBlockingExceptionTest {

    public static void main(String[] args) {
        launchHabaneroApp(new HjSuspendable() {
            @Override
            public void run() {
                isolated(new HjRunnable() {
                    @Override
                    public void run() {
                        try{
                           async(new HjRunnable() {
                                @Override
                                public void run() {
                                    int x = 10;
                                    System.out.println("Test failed. Expected an exception!");
                                }
                        }); 
                        }
                        catch(IllegalStateException x){
                            System.out.println("Test successful! Failure on illigal async creation");
                        }
                        
                    }
                });
            }
        });
    }
}