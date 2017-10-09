/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package permission.conference;

import static edu.rice.hj.Module1.*;
import static edu.rice.hj.Module2.isolated;
import edu.rice.hj.api.HjSuspendable;

/**
 *
 * @author kristophermiles
 */
public class LinkedListWithIsolated {

    public static void main(String[] args) {
        launchHabaneroApp(new HjSuspendable() {
            @Override
            public void run() {
                DoublyLinkedListNode2 thirdNode = new DoublyLinkedListNode2(3);
                DoublyLinkedListNode2 secondNode = new DoublyLinkedListNode2(2);
                DoublyLinkedListNode2 firstNode = new DoublyLinkedListNode2(1);

                firstNode.next = secondNode;
                secondNode.prev = firstNode;
                secondNode.next = thirdNode;
                thirdNode.prev = secondNode;

                deleteTwoNodes(firstNode);

            }
        });
    }

    static void deleteTwoNodes(DoublyLinkedListNode2 L) {
        finish(() -> {
            DoublyLinkedListNode2 second = L.next;
            DoublyLinkedListNode2 third = second.next;
            async(() -> {
                second.delete();
            });
            async(() -> {
                third.delete();
            }); // conflicts with previous async
        });
    }
}

class DoublyLinkedListNode2 {

    public int val;

    public DoublyLinkedListNode2(int in) {
        val = in;
    }

    public DoublyLinkedListNode2 prev, next;

    void delete() {
        isolated(() -> { // start of desired mutual exclusion region
            this.prev.next = this.next;
            this.next.prev = this.prev;
        }); // end of desired mutual exclusion region
        // remaining code that doesn’t need mutual exclusion
    }
} // DoublyLinkedListNode
