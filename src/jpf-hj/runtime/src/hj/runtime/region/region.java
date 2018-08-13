package hj.runtime.region;

import hj.lang.Object;
import hj.lang.ValueType;

/**
 *
 * @author kristophermiles
 */
public abstract class region extends Object implements ValueType {

    public static final int UNKNOWN = 0;
    public static final int RANGE = 1;
    public static final int ARBITRARY = 2;
    public static final int BANDED = 3;
    public static final int MULTIDIM = 4;
    public static final int TRIANGULAR = 5;
    public final int rank;
    public final boolean rect;
    public final int base;
    public final boolean baseSet;
    public static final String propertyNames$ = " rank rect base ";

    protected region(int rank, boolean rect, boolean zeroBased) {
        assert (rank >= 1);
        this.rank = rank;
        this.rect = rect;
        this.base = 0;

        this.baseSet = zeroBased;
    }

    protected region(int rank, boolean rect, int base) {
        assert (rank >= 1);
        this.rank = rank;
        this.rect = rect;
        this.base = base;
        this.baseSet = true;
    }

    public abstract int size();

    public abstract region rank(int paramInt);

    public abstract boolean isConvex();

    public abstract int low()
            throws UnsupportedOperationException;

    public abstract int high()
            throws UnsupportedOperationException;

    public abstract region union(region paramregion);

    public abstract region intersection(region paramregion);

    public abstract region difference(region paramregion);

    public abstract region convexHull();

    public point startPoint() {
        throw new UnsupportedOperationException();
    }

    public boolean contains(region r) {
        throw new UnsupportedOperationException();
    }

    public abstract boolean contains(point parampoint);

    public abstract boolean contains(int[] paramArrayOfInt);

    public abstract boolean disjoint(region paramregion);

    public boolean equal(region r) {
        return (contains(r)) && (r.contains(this));
    }

    public abstract int ordinal(point parampoint)
            throws ArrayIndexOutOfBoundsException;

    public abstract point coord(int paramInt)
            throws ArrayIndexOutOfBoundsException;

    public region[] partition(int n, int dim) {
        throw new UnsupportedOperationException();
    }

    public int rank() {
        return this.rank;
    }

    public boolean rect() {
        return this.rect;
    }

    public int base() {
        return this.base;
    }

    public boolean zeroBased() {
        return base() == 0;
    }
}
