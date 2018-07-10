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
public class Add {

    private int[][] A;
    private int[][] B;
    private final static int SIZE = 4;

    public Add(int a_val, int b_val) {
        A = new int[SIZE][SIZE];
        B = new int[SIZE][SIZE];

        for (int i = 0; i < SIZE; i++) {
            acquireW(A[i], 0, SIZE - 1);
            for (int j = 0; j < SIZE; j++) {
                A[i][j] = a_val;
            }
            releaseW(A[i], 0, SIZE - 1);
        }

        for (int i = 0; i < SIZE; i++) {
            acquireW(B[i], 0, SIZE - 1);
            for (int j = 0; j < SIZE; j++) {
                B[i][j] = b_val;
            }
            releaseW(B[i], 0, SIZE - 1);
        }

    }

    public void add() {
        for (int i = 0; i < A.length; i++) {
            final int[] a_row = A[i];
            final int[] b_row = B[i];
            async(new HjRunnable() {
                public void run() {
                    acquireW(b_row, 0, (SIZE / 2) - 1);
                    acquireR(a_row, 0, (SIZE / 2) - 1);
                    for (int j = 0; j < SIZE / 2; j++) {
                        b_row[j] = b_row[j] + a_row[j];
                    }
                    releaseR(a_row, 0, (SIZE / 2) - 1);
                    releaseW(b_row, 0, (SIZE / 2) - 1);
                }
            });
            async(new HjRunnable() {
                public void run() {
                    acquireW(b_row, SIZE / 2, SIZE - 1);
                    acquireR(a_row, SIZE / 2, SIZE - 1);
                    for (int j = SIZE / 2; j < SIZE; j++) {
                        b_row[j] = b_row[j] + a_row[j];
                    }
                    releaseR(a_row, SIZE / 2, SIZE - 1);
                    releaseW(b_row, SIZE / 2, SIZE - 1);
                }
            });
        }
    }

    public static void main(String[] args) {
        launchHabaneroApp(new HjSuspendable() {
            public void run() {
                Add obj = new Add(1, 0);
                finish(new HjSuspendable() {
                    public void run() {
                        obj.add();
                    }
                });
            }
        });
    }
}
