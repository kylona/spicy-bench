/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package permission.array;

import static edu.rice.hj.Module1.async;
import static edu.rice.hj.Module1.finish;
import static edu.rice.hj.Module1.launchHabaneroApp;
import static permission.PermissionChecks.*;

/**
 * Class used to test how JPF handles sharedness on primitive arrays.
 *
 * @author Peter Anderson <anderson.peter@byu.edu>
 */
public class PrimitiveArrayNoRace {

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
