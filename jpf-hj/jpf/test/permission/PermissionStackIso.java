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
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Peter Anderson <anderson.peter@byu.edu>
 */
public class PermissionStackIso {

    private final Deque<SNode> stack;

    public PermissionStackIso() {
        stack = new ArrayDeque();
    }

    public static void main(String[] args) {
        launchHabaneroApp(new HjSuspendable() {
            public void run() {
                final PermissionStackIso permitList = new PermissionStackIso();
                async(new HjRunnable() {
                    public void run() {
                        isolated(new HjRunnable() {
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
                        isolated(new HjRunnable() {
                            public void run() {
                                acquireW(permitList);
                                permitList.pop();
                                releaseW(permitList);
                            }
                        });
                    }
                });
            }
        });
    }

    private void push(SNode node) {
        //System.out.println("Pushing Element");
        stack.push(node);
    }

    private void pop() {
        if (!stack.isEmpty()) {
            //System.out.println("Popping Element");
            stack.pop();
        }
    }
}
