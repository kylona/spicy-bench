/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package benchmarks.examples;

import static edu.rice.hj.Module1.async;
import static edu.rice.hj.Module1.launchHabaneroApp;
import static edu.rice.hj.Module2.isolated;
import static permission.PermissionChecks.*;

/**
 *
 * @author Peter Anderson <anderson.peter@byu.edu>
 */
public class ClumpedAcess {

    private static final int[] field = {0};

    public static void main(String[] args) {
        launchHabaneroApp(() -> {
            async(() -> {
                isolated(() -> {
                    acquireW(field, 0);
                    for (int i = 0; i < 1000; i++) {
                        field[0]++;
                    }
                    releaseW(field, 0);
                });
            });
            async(() -> {
                isolated(() -> {
                    acquireR(field, 0);
                    for (int i = 0; i < 1000; i++) {
                        System.out.println(field[0]);
                    }
                    releaseR(field, 0);
                });
            });
        });
    }
}
