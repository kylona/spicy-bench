/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package permission;

import static edu.rice.hj.Module2.*;
import edu.rice.hj.api.HjRunnable;
import edu.rice.hj.api.HjSuspendable;
import static permission.PermissionChecks.acquireW;
import static permission.PermissionChecks.releaseW;

/**
 *
 * @author Peter Anderson <anderson.peter@byu.edu>
 */
public class PermissionStackFin {

    public static void main(String[] args) {
        launchHabaneroApp(new HjSuspendable() {
            public void run() {
                final PermissionStack permitList = new PermissionStack();
                finish(new HjSuspendable() {
                    public void run() {
                        async(new HjRunnable() {
                            public void run() {
                                acquireW(permitList);
                                permitList.push(new SNode());
                                releaseW(permitList);
                            }
                        });
                    }
                });
                async(new HjRunnable() {
                    public void run() {
                        acquireW(permitList);
                        permitList.pop();
                        releaseW(permitList);
                    }
                });
            }
        });
    }
}
