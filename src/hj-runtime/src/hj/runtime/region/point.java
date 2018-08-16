package hj.runtime.region;

import edu.rice.hj.api.HjPoint;

/**
 *
 * @author kristophermiles
 */
public class point implements HjPoint {

    private final int[] rank;

    protected point(int rank) {
        this.rank = new int[1];
        this.rank[0] = rank;
    }

    protected point(int[] rank) {
        this.rank = rank;
    }

    @Override
    public int get(int index) {
        return rank[index];
    }

    @Override
    public int rank() {
        return rank.length;
    }

    @Override
    public int compareTo(HjPoint in) {
        point inPoint = (point) in;
        if (inPoint.get(0) > rank[0]) {
            return -1;
        } else if (rank[0] > inPoint.get(0)) {
            return 1;
        }
        return 0;
    }

    private point toPoint(int rank, int c) {
        int[] out = new int[rank];

        for (int i = 0; i < rank; i++) {
            out[i] = c;
        }
        return new point(out);
    }

    public point add(point in) {
        for (int i = 0; i < rank(); i++) {
            rank[i] += in.get(i);
        }
        return this;
    }

    public point add(int c) {
        return add(toPoint(this.rank(), c));
    }

    public point sub(int c) {
        return add(-c);
    }

    public point mul(point in) {
        for (int i = 0; i < rank(); i++) {
            rank[i] *= in.get(i);
        }
        return this;
    }

    public point mul(int c) {
        return mul(toPoint(this.rank(), c));
    }

    public point div(point in) {
        for (int i = 0; i < rank(); i++) {
            rank[i] /= in.get(i);
        }
        return this;
    }

    public point div(int c) {
        return div(toPoint(this.rank(), c));
    }

}
