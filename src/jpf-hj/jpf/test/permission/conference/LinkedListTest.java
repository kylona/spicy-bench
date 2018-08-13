/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package permission.conference;

import static edu.rice.hj.Module1.*;
import edu.rice.hj.api.HjSuspendable;
import static permission.PermissionChecks.acquireR;
import static permission.PermissionChecks.acquireW;

import static permission.PermissionChecks.releaseR;
import static permission.PermissionChecks.releaseW;

/**
 *
 * @author kristophermiles
 */
public class LinkedListTest {

    public static void main(String[] args) {
        launchHabaneroApp(new HjSuspendable() {
            @Override
            public void run() {
                DoublyLinkedListNode thirdNode = new DoublyLinkedListNode(3);
                DoublyLinkedListNode secondNode = new DoublyLinkedListNode(2);
                DoublyLinkedListNode firstNode = new DoublyLinkedListNode(1);

                firstNode.next = secondNode;
                secondNode.prev = firstNode;
                secondNode.next = thirdNode;
                thirdNode.prev = secondNode;

                deleteTwoNodes(firstNode);

            }
        });
    }

    static void deleteTwoNodes(DoublyLinkedListNode L) {
        finish(() -> {
            acquireR(L, 0);
            DoublyLinkedListNode second = L.next;
            DoublyLinkedListNode third = second.next;
            releaseR(L, 0);
            async(() -> {
                acquireW(second, 0);
                second.delete();
                releaseW(second, 0);
            });
            async(() -> {
                acquireW(third, 0);
                third.delete();
                releaseW(third, 0);
            }); // conflicts with previous async
        });
    }
}

class DoublyLinkedListNode {

    public DoublyLinkedListNode prev, next;

    public int value;

    public DoublyLinkedListNode(int in) {
        value = in;
    }

    void delete() {
        { // start of desired mutual exclusion region
            this.prev.next = this.next;
            this.next.prev = this.prev;
        } // end of desired mutual exclusion region
        // remaining code that doesn’t need mutual exclusion
    }
} // DoublyLinkedListNode
