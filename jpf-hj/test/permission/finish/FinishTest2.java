/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package permission.finish;

import static edu.rice.hj.Module1.*;
import edu.rice.hj.api.HjRunnable;
import edu.rice.hj.api.HjSuspendable;
import static permission.PermissionChecks.*;

/**
 *
 * @author Peter Anderson <anderson.peter@byu.edu>
 */
public class FinishTest2 {

    public static void main(String[] args) {
        launchHabaneroApp(new HjSuspendable() {
            public void run() {
                FinishTest2 obj = new FinishTest2();
                FinishTest2.Box box = obj.new Box();
                async(new HjRunnable() {
                    public void run() {
                        acquireW(obj);
                        box.field = 1;
                        releaseW(obj);
                    }
                });
                finish(new HjSuspendable() {
                    public void run() {
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
                        async(new HjRunnable() {
                            public void run() {
                                acquireR(obj);
                                System.out.println(box.field);
                                releaseR(obj);
                            }
                        });
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
