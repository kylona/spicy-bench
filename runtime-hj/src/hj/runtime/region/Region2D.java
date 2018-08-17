/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hj.runtime.region;

import edu.rice.hj.api.HjPoint;
import edu.rice.hj.api.HjRegion2D;
import java.util.Iterator;

/**
 *
 * @author kristophermiles
 */
public class Region2D extends AbstractRegion implements HjRegion2D {

    private int min0, max0, min1, max1, range0, range1;

    Region2D(int min0, int max0, int min1, int max1) {
        this.min0 = min0;
        this.max0 = max0;
        this.min1 = min1;
        this.max1 = max1;

        range0 = this.max0 - this.min0 + 1;
        range1 = this.max1 - this.min0 + 1;
    }

    public int rank() {
        return 2;
    }

    @Override
    public int[] lowerLimits() {
        return new int[]{this.min0, this.min1};

    }

    @Override
    public int toLinearIndex(int regionIndex0, int regionIndex1) {
        int index0 = regionIndex0 - this.min0;
        int index1 = regionIndex1 - this.min1;
        return index0 * this.range1 + index1;
    }

    @Override
    public int toLinearIndex(int[] regionIndices) {
        return toLinearIndex(regionIndices[0], regionIndices[1]);
    }

    @Override
    public int[] upperLimits() {
        return new int[]{this.max0, this.max1};
    }

    @Override
    public Iterator<HjPoint> iterator() {
        return iterator(0, numElements());
    }

    @Override
    public Iterator<HjPoint> iterator(int start, int max) {
        int[] lower = {this.min0, this.min1};
        int[] upper = {this.max0, this.max1};

        return createIterator(start, max, lower, upper);
    }

    @Override
    public int lowerLimit(int index) {
        if (index == 0) {
            return min0;
        } else {
            return min1;
        }
    }

    @Override
    public int numElements() {
        return range0 * range1;
    }

    @Override
    public int[] toRegionIndex(int linearIndex) {
        int[] out = new int[rank()];

        out[0] = (linearIndex / this.range1 + this.min0);
        out[1] = ((linearIndex + this.range1) % this.range1 + this.min1);

        return out;
    }

    @Override
    public Iterable<HjPoint> toSeqIterable() {
        return new Iterable() {
            @Override
            public Iterator<HjPoint> iterator() {
                return Region2D.this.iterator();
            }
        };
    }

    @Override
    public int upperLimit(int index) {
        if (index == 0) {
            return max0;
        } else {
            return max1;
        }
    }
}
