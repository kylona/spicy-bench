/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package carTest;

import static carTest.Car.doWork;
import static edu.rice.hj.Module1.async;
import edu.rice.hj.api.HjRunnable;

/**
 *
 * @author Kris
 */
public class ComponentsFactory {
        public static void installWindshield() {
        async(new HjRunnable() {
            @Override
            public void run() {
                doWork();
                System.out.println("Windshield is installed, boop boop.");
            }
        });
    }

    public static void paintCar() {
        async(new HjRunnable() {
            @Override
            public void run() {
                doWork();
                System.out.println("Car is painted, boop boop.");
            }
        });
    }
}
