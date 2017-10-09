package hj.runtime.region;

/**
 *
 * @author kristophermiles
 */
import edu.rice.hj.api.HjRegion3D;
import edu.rice.hj.api.HjRegion2D;
import edu.rice.hj.api.HjRegion1D;
import java.util.ArrayList;
import java.util.List;

public final class RegionFactory {

    public static HjRegion1D new1D(int min, int max) {
        return new Region1D(min, max);
    }

    public static HjRegion2D new2D(int min0, int max0, int min1, int max1) {
        return new Region2D(min0, max0, min1, max1);
    }

    public static HjRegion3D new3D(int min0, int max0, int min1, int max1, int min2, int max2) {
        return new Region3D(min0, max0, min1, max1, min2, max2);
    }

    public static List<HjRegion1D> groupRegion(HjRegion1D hjRegion1D, int processorGrid) {
        int lower0 = hjRegion1D.lowerLimits()[0];
        int upper0 = hjRegion1D.upperLimits()[0];
        int increment0 = (upper0 - lower0 + 1) / processorGrid;

        List result = new ArrayList();

        for (int i = lower0; i < upper0; i += increment0) {
            int start0 = i;
            int end0 = Math.min(upper0, i + increment0);
            HjRegion1D loopRegion = new1D(start0, end0);
            result.add(loopRegion);
        }

        return result;
    }

    public static HjRegion1D myGroup(int groupId, HjRegion1D hjRegion1D, int groupSize) {
        int lower0 = hjRegion1D.lowerLimits()[0];
        int upper0 = hjRegion1D.upperLimits()[0];
        int range0 = upper0 - lower0 + 1;
        int increment0 = range0 / groupSize + (range0 % groupSize == 0 ? 0 : 1);

        int start0 = groupId * increment0 + lower0;
        int end0 = Math.min(upper0, start0 + increment0 - 1);
        return new1D(start0, end0);
    }

    public static List<HjRegion2D> groupRegion(HjRegion2D hjRegion2D, int processorGrid0, int processorGrid1) {
        int lower0 = hjRegion2D.lowerLimits()[0];
        int upper0 = hjRegion2D.upperLimits()[0];
        int increment0 = (upper0 - lower0 + 1) / processorGrid0;

        int lower1 = hjRegion2D.lowerLimits()[1];
        int upper1 = hjRegion2D.upperLimits()[1];
        int increment1 = (upper1 - lower1 + 1) / processorGrid1;

        List result = new ArrayList();

        for (int i = lower0; i < upper0; i += increment0) {
            int start0 = i;
            int end0 = Math.min(upper0, i + increment0);

            for (int j = lower1; j < upper1; j += increment1) {
                int start1 = j;
                int end1 = Math.min(upper1, j + increment1);
                HjRegion2D loopRegion = new2D(start0, end0, start1, end1);
                result.add(loopRegion);
            }
        }

        return result;
    }

    public static HjRegion2D myGroup(int groupId0, int groupId1, HjRegion2D hjRegion2D, int groupSize0, int groupSize1) {
        int[] lowerLimits = hjRegion2D.lowerLimits();
        int[] upperLimits = hjRegion2D.upperLimits();

        int lower0 = lowerLimits[0];
        int upper0 = upperLimits[0];
        int range0 = upper0 - lower0 + 1;
        int increment0 = range0 / groupSize0 + (range0 % groupSize0 == 0 ? 0 : 1);

        int lower1 = lowerLimits[1];
        int upper1 = upperLimits[1];
        int range1 = upper1 - lower1 + 1;
        int increment1 = range1 / groupSize1 + (range1 % groupSize1 == 0 ? 0 : 1);

        int start0 = groupId0 * increment0 + lower0;
        int end0 = Math.min(upper0, start0 + increment0 - 1);

        int start1 = groupId1 * increment1 + lower1;
        int end1 = Math.min(upper1, start1 + increment1 - 1);

        return new2D(start0, end0, start1, end1);
    }

    private RegionFactory() {
        throw new IllegalStateException("Utility class should not be instantiated!");
    }
}
