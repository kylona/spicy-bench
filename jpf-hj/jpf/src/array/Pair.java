/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package array;

/**
 *
 * @author Peter Anderson <anderson.peter@byu.edu>
 */
public class Pair<K, V> {

    private final K first;
    private final V second;

    public Pair(K first, V second) {
        this.first = first;
        this.second = second;
    }

    public K getValue0() {
        return first;
    }

    public V getValue1() {
        return second;
    }
}
