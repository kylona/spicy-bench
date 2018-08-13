/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lib.runtime.region;

import edu.rice.hj.api.HjProcedureInt3D;

/**
 *
 * @author kristophermiles
 */
public class ThreeDimentionalTestProcedure implements HjProcedureInt3D {

    private int out0, out1, out2;

    public ThreeDimentionalTestProcedure() {
        out0 = 0;
        out1 = 0;
        out2 = 0;
    }

    public int getResult0() {
        return out0;
    }

    public int getResult1() {
        return out1;
    }

    public int getResult2() {
        return out2;
    }

    @Override
    public void apply(int paramInt1, int paramInt2, int paramInt3) {
        out0 += paramInt1;
        out1 += paramInt2;
        out2 += paramInt3;
    }
}
