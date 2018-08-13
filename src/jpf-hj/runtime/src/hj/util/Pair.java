/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hj.util;

/**
 *
 * @author kristophermiles
 */
public class Pair<L, R> {

    public final L left;
    public final R right;

    public static <L, R> Pair<L, R> factory(L left, R right) {
        return new Pair(left, right);
    }

    protected static boolean equalsHelper(Object o1, Object o2) {
        return (o1 == o2) || (o1.equals(o2));
    }

    protected static int hashCodeHelper(Object o1) {
        return o1 == null ? 0 : o1.hashCode();
    }

    public Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public int hashCode() {
        return hashCodeHelper(this.left) + hashCodeHelper(this.right);
    }

    @Override
    public boolean equals(Object otherObj) {
        if (this == otherObj) {
            return true;
        }
        if (!(otherObj instanceof Pair)) {
            return false;
        }

        Pair other = (Pair) otherObj;
        return (equalsHelper(this.left, other.left)) && (equalsHelper(this.right, other.right));
    }

    @Override
    public String toString() {
        return "Pair(" + this.left + ", " + this.right + ")";
    }
}
