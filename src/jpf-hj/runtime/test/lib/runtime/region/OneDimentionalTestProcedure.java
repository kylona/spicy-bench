/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lib.runtime.region;

import edu.rice.hj.api.HjProcedureInt1D;

/**
 *
 * @author kristophermiles
 */
public class OneDimentionalTestProcedure implements HjProcedureInt1D {

    private int output;

    public OneDimentionalTestProcedure() {
        output = 0;
    }

    @Override
    public void apply(int paramInt) {
        output += paramInt;
        System.out.println("Final value after apply " + output + "\n");
    }

    public int getResult() {
        return output;
    }
}
