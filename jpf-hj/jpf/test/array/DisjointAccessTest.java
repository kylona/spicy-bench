/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package array;

import static edu.rice.hj.Module1.async;
import static edu.rice.hj.Module1.launchHabaneroApp;

/**
 *
 * @author Peter Anderson <anderson.peter@byu.edu>
 *
 */
public class DisjointAccessTest {

    public static final int COUNT = 2;
    private final int[] nums = new int[COUNT];

    public static void main(String[] args) {
        launchHabaneroApp(() -> {
            DisjointAccessTest obj = new DisjointAccessTest();
            for (int i = 0; i < COUNT; i++) {
                final int ii = i;
                async(() -> {
                    obj.accessArray(ii);
                });
            }
        });
    }

    public void accessArray(int index) {
        async(() -> {
            nums[index] = 1;
            //System.out.println(nums[index]);
        });
    }
}
