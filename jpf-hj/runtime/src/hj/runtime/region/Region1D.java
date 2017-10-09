/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hj.runtime.region;

import edu.rice.hj.api.HjPoint;
import edu.rice.hj.api.HjRegion1D;
import java.util.Iterator;

/**
 *
 * @author kristophermiles
 */
public class Region1D extends AbstractRegion implements HjRegion1D {

    protected final int min, max;

    @Override
    public int rank() {
        return 1;
    }

    public Region1D(int min, int max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public int[] lowerLimits() {
        return new int[]{min};
    }

    @Override
    public int toLinearIndex(int regionIndex0) {
        return regionIndex0 - min;
    }

    @Override
    public int toLinearIndex(int[] regionIndices) {
        return regionIndices[0] - min;
    }

    @Override
    public int[] upperLimits() {
        return new int[]{max};
    }

    @Override
    public Iterator<HjPoint> iterator() {
        return iterator(0, numElements());
    }

    @Override
    public Iterator<HjPoint> iterator(int start, int max) {
        int[] lower = {this.min};
        int[] upper = {this.max};

        return createIterator(start, max, lower, upper);
    }

    @Override
    public int lowerLimit(int index) {
        return min;
    }

    @Override
    public int numElements() {
        return (max - min + 1);
    }

    @Override
    public int[] toRegionIndex(int linearIndex) {
        int[] out = new int[1];
        out[0] = (linearIndex + min);
        return out;
    }

    @Override
    public Iterable<HjPoint> toSeqIterable() {
        return new Iterable() {
            @Override
            public Iterator<HjPoint> iterator() {
                return Region1D.this.iterator();
            }
        };
    }

    @Override
    public int upperLimit(int index) {
        return max;
    }

}
