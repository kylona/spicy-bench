/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package extensions;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.jvm.bytecode.ArrayLoadInstruction;
import gov.nasa.jpf.jvm.bytecode.ArrayStoreInstruction;
import gov.nasa.jpf.vm.ApplicationContext;
import gov.nasa.jpf.vm.ArrayFields;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.FieldInfo;
import gov.nasa.jpf.vm.GlobalSharednessPolicy;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.ThreadList;
import gov.nasa.jpf.vm.choice.ThreadChoiceFromSet;
import array.ArrayMonitor;
import java.util.Random;

/**
 *
 * @author Peter Anderson <anderson.peter@byu.edu>
 */
public class HjSharednessPolicy extends GlobalSharednessPolicy {

    private static final Random randNumGen = new Random(0);

    public HjSharednessPolicy(Config config) {
        super(config);
    }

    @Override
    public boolean canHaveSharedObjectCG(ThreadInfo ti, Instruction insn, ElementInfo eiFieldOwner, FieldInfo fi) {
        return false;
    }

    @Override
    public boolean canHaveSharedClassCG(ThreadInfo ti, Instruction insn, ElementInfo eiFieldOwner, FieldInfo fi) {
        return false;
    }

    private boolean validMethod(String methodName) {
        String[] invalidPackages = {
            "java.util",
            "hj.lang",
            "hj.runtime",
            "hj.util",
            "java.lang"
        };
        for (String name : invalidPackages) {
            if (methodName.contains(name)) {
                return false;
            }
        }
//        System.out.println(methodName);
        return true;
    }

    @Override
    public boolean canHaveSharedArrayCG(ThreadInfo ti, Instruction insn, ElementInfo eiArray, int idx) {
        MethodInfo mi = insn.getMethodInfo();
        String baseName = mi.getBaseName();
        return validMethod(baseName);
    }

    @Override
    public boolean setsSharedObjectCG(ThreadInfo ti, Instruction i, ElementInfo ei, FieldInfo fi) {
        return false;
    }

    @Override
    public boolean setsSharedClassCG(ThreadInfo ti, Instruction insn, ElementInfo ei, FieldInfo fi) {
        return false;
    }

    @Override
    public boolean setsSharedArrayCG(ThreadInfo ti, Instruction insn, ElementInfo eiArray, int index) {
        return false;
    }

    @Override
    protected boolean setsExposureCG(ThreadInfo ti, Instruction insn, ElementInfo eiFieldOwner, FieldInfo fi, ElementInfo eiExposed) {
        return false;
    }

    protected boolean setNextChoiceGenerator(ChoiceGenerator<ThreadInfo> cg) {
        if (cg == null) {
            return false;
        } else {
            return vm.getSystemState().setNextChoiceGenerator(cg);
        }
    }

    protected ChoiceGenerator<ThreadInfo> getSingleChoiceCG(String id, ThreadInfo tiCurrent) {
        //In cases where there are multiple runnables a random choice is made
        ThreadInfo[] timeoutRunnables
                = getTimeoutRunnables(tiCurrent.getApplicationContext());
        if (timeoutRunnables.length == 0) {
            return null;
        } else if (timeoutRunnables.length == 1) {
            return new ThreadChoiceFromSet(id, timeoutRunnables, true);
        } else {
            int nextChoiceIndex = randNumGen.nextInt(timeoutRunnables.length);
            ThreadInfo[] choice = {timeoutRunnables[nextChoiceIndex]};
            return new ThreadChoiceFromSet(id, choice, true);
        }
    }

    protected ThreadInfo[] getTimeoutRunnables(ApplicationContext appCtx) {
        ThreadList tlist = vm.getThreadList();
        if (tlist.hasProcessTimeoutRunnables(appCtx)) {
            return tlist.getProcessTimeoutRunnables(appCtx);
        } else {
            return tlist.getTimeoutRunnables();
        }
    }

    @Override
    public ElementInfo updateArraySharedness(ThreadInfo ti, ElementInfo ei,
            int index) {
        //System.out.println("Updating sharedness on index: " + index);
        Object attributeInfo = ei.getObjectAttr(ArrayMonitor.class);
        ArrayMonitor oldMonitor;
        if (attributeInfo == null) {
            ArrayFields arrayFields = ei.getArrayFields();
            int len = arrayFields.arrayLength();
            oldMonitor = new ArrayMonitor(len);
        } else {
//            System.out.println("Retained old monitor");
            oldMonitor = (ArrayMonitor) attributeInfo;
        }
        ArrayMonitor newMonitor = null;
        if (ti.getPC() instanceof ArrayStoreInstruction) {
            newMonitor = oldMonitor.addWriter(index, ti.getId());
        } else if (ti.getPC() instanceof ArrayLoadInstruction) {
            newMonitor = oldMonitor.addReader(index, ti.getId());
        }

        if (newMonitor != null) {
            ei = ei.getModifiableInstance();
            if (attributeInfo == null) {
                ei.addObjectAttr(newMonitor);
            } else {
                ei.replaceObjectAttr(oldMonitor, newMonitor);
            }
            if (newMonitor.isShared() && !ei.isShared()
                    && !ei.isSharednessFrozen()) {
                ei.setShared(ti, true);
            }
        }
        return ei;
    }

}
