/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hj.runtime.region;

import edu.rice.hj.api.HjPoint;
import edu.rice.hj.api.HjRegion;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 *
 * @author kristophermiles
 */
public abstract class AbstractRegion implements HjRegion {

    protected Iterator<HjPoint> createIterator(final int startLinearIndex, int maxNumItems, final int[] lower, final int[] upper) {
        int itemsUpperBound = Math.min(numElements(), startLinearIndex + maxNumItems);
        final int maxItems = Math.max(0, itemsUpperBound - startLinearIndex);

        return new Iterator() {
            final int[] iterItem = AbstractRegion.this.toRegionIndex(startLinearIndex);
            final HjPoint innerPointInstance = PointFactory.point(this.iterItem);
            int counter = 0;

            @Override
            public boolean hasNext() {
                return this.counter < maxItems;
            }

            @Override
            public HjPoint next() {
                if (!hasNext()) {
                    throw new NoSuchElementException("No element exists past: " + Arrays.toString(this.iterItem) + ", counter is " + this.counter);
                }
                if (this.counter > 0) {
                    for (int i = AbstractRegion.this.rank() - 1; i >= 0; i--) {
                        int newValue = this.iterItem[i] + 1;
                        if (newValue > upper[i]) {
                            this.iterItem[i] = lower[i];
                        } else {
                            this.iterItem[i] = newValue;
                            break;
                        }
                    }

                }

                this.counter += 1;

                return this.innerPointInstance;

            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("remove");
            }
        };
    }
}
