/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package extensions;

import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.choice.*;

/**
 *
 * @author bchase
 * @author Peter Anderson <anderson.peter@byu.edu>
 */
public class HjThreadChoice extends ThreadChoiceFromSet {

    public HjThreadChoice(String id, ThreadInfo[] set, boolean isSchedulingPoint) {
        super(id, set, isSchedulingPoint);
    }
}
