/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package permission;

import static edu.rice.hj.Module1.*;
import static permission.PermissionChecks.*;
import edu.rice.hj.api.HjRunnable;
import edu.rice.hj.api.HjSuspendable;
import java.util.Stack;

/**
 *
 * @author Peter Anderson <anderson.peter@byu.edu>
 */
public class PermissionStack {

    private final Stack<SNode> stack;

    public PermissionStack() {
        stack = new Stack();
        stack.push(new SNode());
    }

    public static void main(String[] args) {
        launchHabaneroApp(new HjSuspendable() {
            public void run() {
                final PermissionStack permitList = new PermissionStack();
                async(new HjRunnable() {
                    public void run() {
                        acquireR(permitList);
                        permitList.push(new SNode());
                        releaseR(permitList);
                    }
                });
                async(new HjRunnable() {
                    public void run() {
                        acquireR(permitList);
                        permitList.peek();
                        releaseR(permitList);
                    }
                });
            }
        });
    }

    public void push(SNode node) {
        stack.push(node);
    }

    public SNode pop() {
        return stack.pop();
    }

    public void peek() {
        stack.peek();
    }
}

class SNode {

    protected SNode() {
    }
}
