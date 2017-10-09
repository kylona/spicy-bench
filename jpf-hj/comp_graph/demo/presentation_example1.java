/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demo;

import static demo.demo_example1.a;
import static edu.rice.hj.Module1.async;
import static edu.rice.hj.Module1.launchHabaneroApp;
import static edu.rice.hj.Module2.isolated;
import edu.rice.hj.api.HjRunnable;
import edu.rice.hj.api.HjSuspendable;
import edu.rice.hj.api.SuspendableException;
import java.util.Random;

/**
 *
 * @author kristophermiles
 */
public class presentation_example1 {

    static int global = 0;

    public static void main(String[] args) {
        launchHabaneroApp(new HjSuspendable() {
            @Override
            public void run() throws SuspendableException {
                async(new HjRunnable() {
                    @Override
                    public void run() {
                        isolated(new HjRunnable() {
                            public void run() {
                                a = 2;
                                System.out.println("a = " + a);
                            }
                        });

                    }
                });

                async(new HjRunnable() {
                    @Override
                    public void run() {
                        isolated(new HjRunnable() {
                            public void run() {
                                a = 5;
                            }
                        });
                    }
                });

                /*for(int i=0;i<3;i++){
                 final int n =i;
                 async(new HjRunnable() {
                 @Override
                 public void run() {
                 global++;
                 }
                 });
                 }*/
            }
        });
    }
}
