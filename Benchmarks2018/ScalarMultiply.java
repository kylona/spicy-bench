/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import static edu.rice.hj.Module1.*;
import edu.rice.hj.api.*;
import static permission.PermissionChecks.*;

/**
 *
 * @author Peter Anderson <anderson.peter@byu.edu>
 */
public class ScalarMultiply {

    public final static int SIZE = 10;
    private int[][] M;

    public ScalarMultiply(int val) {
        M = new int[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            acquireW(M[i], 0, SIZE - 1);
            for (int j = 0; j < SIZE; j++) {
                M[i][j] = val;
            }
            releaseW(M[i], 0, SIZE - 1);
        }
    }

    public void multiply(int scalar) {
        for (int j = 0; j < SIZE; j++) {
            final int[] row = M[j];
            async(new HjRunnable() {
                public void run() {
                    acquireW(row, 0, row.length - 1);
                    for (int i = 0; i < row.length; i++) {
                        row[i] = row[i] * scalar;
                    }
                    releaseW(row, 0, row.length - 1);
                }
            });
        }
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (int[] row : M) {
            for (int num : row) {
                builder.append(num);
                builder.append(' ');
            }
            builder.append('\n');
        }
        return builder.toString();
    }

    public static void main(String[] args) {
        launchHabaneroApp(new HjSuspendable() {
            public void run() {
                ScalarMultiply obj = new ScalarMultiply(1);
                finish(new HjSuspendable() {
                    public void run() {
                        obj.multiply(2);
                    }
                });
//                System.out.println(obj.toString());
            }
        });
    }
}
