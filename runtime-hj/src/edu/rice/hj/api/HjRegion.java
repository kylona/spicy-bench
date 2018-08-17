package edu.rice.hj.api;

import java.util.Iterator;

/**
 *
 * @author kristophermiles
 */
public interface HjRegion {

    public Iterator<HjPoint> iterator();

    public Iterator<HjPoint> iterator(int start, int max);

    public int lowerLimit(int index);

    public int numElements();

    public int rank();

    public int toLinearIndex(int[] regionIndices);

    public int[] toRegionIndex(int linearIndex);

    public Iterable<HjPoint> toSeqIterable();

    public int upperLimit(int index);

}
