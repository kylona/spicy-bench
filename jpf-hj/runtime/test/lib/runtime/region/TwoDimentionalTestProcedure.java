/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lib.runtime.region;

import edu.rice.hj.api.HjProcedureInt2D;

/**
 *
 * @author kristophermiles
 */
public class TwoDimentionalTestProcedure implements HjProcedureInt2D {

    private int out0, out1;

    public TwoDimentionalTestProcedure() {
        out0 = 0;
        out1 = 0;
    }

    public int getResult0() {
        return out0;
    }

    public int getResult1() {
        return out1;
    }

    @Override
    public void apply(int paramInt1, int paramInt2) {
        out0 += paramInt1;
        out1 += paramInt2;
    }
}
