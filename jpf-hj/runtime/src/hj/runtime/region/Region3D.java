/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hj.runtime.region;

import edu.rice.hj.api.HjPoint;
import edu.rice.hj.api.HjRegion3D;
import java.util.Iterator;

/**
 *
 * @author kristophermiles
 */
public class Region3D extends AbstractRegion implements HjRegion3D {

    private int min0, max0, min1, max1, min2, max2;
    private int range0, range1, range2;

    Region3D(int min0, int max0, int min1, int max1, int min2, int max2) {
        this.min0 = min0;
        this.max0 = max0;
        this.min1 = min1;
        this.max1 = max1;
        this.min2 = min2;
        this.max2 = max2;

        this.range0 = (this.max0 - this.min0 + 1);
        this.range1 = (this.max1 - this.min1 + 1);
        this.range2 = (this.max2 - this.min2 + 1);
    }

    public int rank() {
        return 3;
    }

    @Override
    public int[] lowerLimits() {
        return new int[]{this.min0, this.min1, this.min2};
    }

    @Override
    public int toLinearIndex(int regionIndex0, int regionIndex1, int regionIndex2) {
        int index0 = regionIndex0 - this.min0;
        int index1 = regionIndex1 - this.min1;
        int index2 = regionIndex2 - this.min2;

        return index0 * this.range1 * this.range2 + index1 * this.range2 + index2;
    }

    @Override
    public int toLinearIndex(int[] regionIndices) {
        return toLinearIndex(regionIndices[0], regionIndices[1], regionIndices[2]);
    }

    @Override
    public int[] upperLimits() {
        return new int[]{this.max0, this.max1, this.max2};
    }

    @Override
    public Iterator<HjPoint> iterator() {
        return iterator(0, numElements());
    }

    @Override
    public Iterator<HjPoint> iterator(int start, int max) {
        int[] lower = {this.min0, this.min1, this.min2};
        int[] upper = {this.max0, this.max1, this.max2};

        return createIterator(start, max, lower, upper);
    }

    @Override
    public int lowerLimit(int index) {
        if (index == 0) {
            return min0;
        } else if (index == 1) {
            return min1;
        } else {
            return min2;
        }
    }

    @Override
    public int numElements() {
        return range0 * range1 * range2;
    }

    @Override
    public int[] toRegionIndex(int linearIndex) {
        int box1 = this.range1 * this.range2;
        int remItems1 = (linearIndex + box1) % box1;

        int[] out = new int[rank()];

        out[0] = (linearIndex / box1 + this.min0);
        out[1] = (remItems1 / this.range2 + this.min1);
        out[2] = ((remItems1 + this.range2) % this.range2 + this.min2);

        return out;
    }

    @Override
    public Iterable<HjPoint> toSeqIterable() {
        return new Iterable() {
            @Override
            public Iterator<HjPoint> iterator() {
                return Region3D.this.iterator();
            }
        };
    }

    @Override
    public int upperLimit(int index) {
        if (index == 0) {
            return max0;
        } else if (index == 1) {
            return max1;
        } else {
            return max2;
        }
    }

}
