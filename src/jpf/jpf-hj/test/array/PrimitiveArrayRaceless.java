/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package array;

import static edu.rice.hj.Module0.finish;
import static edu.rice.hj.Module0.launchHabaneroApp;
import static edu.rice.hj.Module1.async;

/**
 *
 * @author kristophermiles
 */
public class PrimitiveArrayRaceless {
    public static final int SIZE = 10;

    public static void main(String[] args) {
        launchHabaneroApp(() -> {
            int[] primitives = new int[SIZE];
            finish(() -> {
                async(() -> {
                    for (int i = 0; i < SIZE / 2; i++) {
                        primitives[i] = i;
                    }
                });
                async(() -> {
                    for (int i = SIZE / 2; i < SIZE; i++) {
                        primitives[i] = i;
                    }
                });
            });
        });
    }
}

