/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hj.runtime.region;

import edu.rice.hj.api.HjPoint;
import hj.lang.ValueType;

/**
 *
 * @author kristophermiles
 */
public class PointFactory implements ValueType {

    public static HjPoint point(int v1) {
        return new point(new int[]{v1});
    }

    public static HjPoint allZero(int d) {
        return new point(new int[d]);
    }

    public static HjPoint point(int v1, int v2) {
        return new point(new int[]{v1, v2});
    }

    public static HjPoint point(int v1, int v2, int v3) {
        return new point(new int[]{v1, v2, v3});
    }

    public static HjPoint point(int v1, int v2, int v3, int v4) {
        return new point(new int[]{v1, v2, v3, v4});
    }

    public static HjPoint point(int v1, int v2, int v3, int v4, int v5) {
        return new point(new int[]{v1, v2, v3, v4, v5});
    }

    public static HjPoint point(int[] in) {
        return new point(in);
    }
}
