/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package permission.finish;

import static edu.rice.hj.Module1.*;
import edu.rice.hj.api.HjSuspendable;
import edu.rice.hj.api.HjRunnable;
import static permission.PermissionChecks.*;

/**
 *
 * @author Peter Anderson <anderson.peter@byu.edu>
 */
public class FinishTest1 {

    public static void main(String[] args) {
        launchHabaneroApp(new HjSuspendable() {
            public void run() {
                FinishTest1 obj = new FinishTest1();
                FinishTest1.Box box = obj.new Box();
                finish(new HjSuspendable() {
                    public void run() {
                        async(new HjRunnable() {
                            public void run() {
                                acquireR(box);
                                System.out.println(box.field);
                                releaseR(box);
                            }
                        });
                        async(new HjRunnable() {
                            public void run() {
                                acquireR(obj);
                                System.out.println(box.field);
                                releaseR(obj);
                            }
                        });
                        async(new HjRunnable() {
                            public void run() {
                                acquireR(obj);
                                System.out.println(box.field);
                                releaseR(obj);
                            }
                        });
                    }
                });
                async(new HjRunnable() {
                    public void run() {
                        acquireW(obj);
                        System.out.println(box.field);
                        releaseW(obj);
                    }
                });
            }
        });
    }

    public class Box {

        public int field;

        public Box() {
            field = 0;
        }
    }
}
