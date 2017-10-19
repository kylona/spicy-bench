package extensions;

import gov.nasa.jpf.*;
import gov.nasa.jpf.jvm.bytecode.JVMInvokeInstruction;
import gov.nasa.jpf.jvm.bytecode.JVMReturnInstruction;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.vm.*;
import gov.nasa.jpf.vm.choice.ThreadChoiceFromSet;
import permission.OPTSupport;
import permission.PermissionError;
import permission.PermissionTracker;

/**
 *
 * @author bchase
 * @author Peter Anderon <anderson.peter@byu.edu>
 *
 */
public class HjListener extends PropertyListenerAdapter implements Property {

    private final String readAcquire
            = "permission.PermissionChecks.acquireR";
    private final String writeAcquire
            = "permission.PermissionChecks.acquireW";
    private final String readRelease
            = "permission.PermissionChecks.releaseR";
    private final String writeRelease
            = "permission.PermissionChecks.releaseW";
    private final String isolated
            = "edu.rice.hj.Module2.isolated";
    private boolean permissionError;
    private String permissionExplanation;

    private final String[] ignored = {
        "hj.lang",
        "edu.rice",
        "java.util",
        "java.lang",
        "hj.util"
    };

    public HjListener() {
        super();
        permissionError = false;
        permissionExplanation = "";
        System.out.println("Started HjListener");
    }

    protected ThreadInfo[] getTimeoutRunnables(VM vm,
            ApplicationContext appCtx) {
        ThreadList tlist = vm.getThreadList();
        if (tlist.hasProcessTimeoutRunnables(appCtx)) {
            return tlist.getProcessTimeoutRunnables(appCtx);
        } else {
            return tlist.getTimeoutRunnables();
        }
    }

    protected ChoiceGenerator<ThreadInfo> getRunnableCG(String id,
            ThreadInfo tiCurrent, VM vm) {
        ThreadInfo[] timeoutRunnables
                = getTimeoutRunnables(vm, tiCurrent.getApplicationContext());
        if (timeoutRunnables.length == 0) {
            return null;
        } else if (timeoutRunnables.length == 1
                && timeoutRunnables[0] == tiCurrent) {
            return null;
        } else {
            return new ThreadChoiceFromSet(id, timeoutRunnables, true);
        }
    }

    @Override
    public void executeInstruction(VM vm, ThreadInfo currentThread,
            Instruction instructionToExecute) {
        if (instructionToExecute instanceof JVMReturnInstruction) {
            MethodInfo mi = instructionToExecute.getMethodInfo();
            String baseName = mi.getBaseName();
            String choiceType;
            if (isAcquireMethod(baseName)) {
                choiceType = "PERMISSION";
                ChoiceGenerator<ThreadInfo> cg
                        = getRunnableCG(choiceType, currentThread, vm);
                vm.getSystemState().setNextChoiceGenerator(cg);
            } else {
                return;
            }
        } else if (instructionToExecute instanceof JVMInvokeInstruction) {
            MethodInfo mi = ((JVMInvokeInstruction) instructionToExecute).getInvokedMethod();
            String baseName = mi.getBaseName();
            String choiceType;
            if (isIsolatedMethod(baseName)) {
                choiceType = "ISOLATED";
                throw new Throwable();
                ChoiceGenerator<ThreadInfo> cg
                        = getRunnableCG(choiceType, currentThread, vm);
                vm.getSystemState().setNextChoiceGenerator(cg);
            } else {
                return;
            }
        }
    }

    @Override
    public void choiceGeneratorRegistered(VM vm, ChoiceGenerator<?> nextCG,
            ThreadInfo currentThread, Instruction executedInstruction) {
        System.out.println("Line 105 of HjListener prints: " + nextCG.getId());
    }

    private boolean isPermissionMethod(String methodName) {
        return methodName.equals(readAcquire)
                || methodName.equals(writeAcquire)
                || methodName.equals(readRelease)
                || methodName.equals(writeRelease);
    }

    private boolean isAcquireMethod(String methodName) {
        return methodName.equals(readAcquire)
                || methodName.equals(writeAcquire);
    }

    private boolean isIsolatedMethod(String methodName) {
        return methodName.equals(isolated);
    }

    private boolean validInstruction(Instruction insn) {
        return insn instanceof JVMInvokeInstruction;
    }

    private boolean validMethod(JVMInvokeInstruction invoked, ThreadInfo ti) {
        String baseName = extractMethodName(invoked, ti);
        return isPermissionMethod(baseName);
    }

    private String extractMethodName(JVMInvokeInstruction invoked,
            ThreadInfo ti) {
        MethodInfo mi = invoked.getInvokedMethod(ti);
        return mi.getBaseName();
    }

    private void stateTransitionOnObject(Object[] args, ThreadInfo ti,
            String methodName) {
        Object arg = args[0];
        if (arg instanceof ElementInfo) {
            ElementInfo ei = (ElementInfo) arg;
            PermissionTracker oldAttr = OPTSupport.withDefault(
                    ei.getObjectAttr(PermissionTracker.class));
            PermissionTracker newAttr = null;
            switch (methodName) {
                case readAcquire:
                    newAttr = oldAttr.acquireR(ti);
                    break;
                case writeAcquire:
                    newAttr = oldAttr.acquireW(ti);
                    break;
                case readRelease:
                    newAttr = oldAttr.releaseR(ti);
                    break;
                case writeRelease:
                    newAttr = oldAttr.releaseW(ti);
                    break;
                default:
                    assert (false);
            }
            ei = ei.getModifiableInstance();
            ei.setObjectAttr(newAttr);
        }
    }

    private void stateTransitionOnArray(Object[] args, ThreadInfo ti,
            String methodName) {
        Object arg0 = args[0];
        Object arg1 = args[1];
        if (arg0 instanceof ElementInfo && arg1 instanceof Integer) {
            ElementInfo ei = (ElementInfo) arg0;
            int index = (Integer) arg1;
//            System.out.println("Processing Array with id: " + ti.getId() +
//                    " and index: " + index);
            if (ei.isArray()) {
                ArrayFields arrayFields = ei.getArrayFields();
                int len = arrayFields.arrayLength();
                PermissionTracker[] oldAttr = OPTSupport.withArrayDefault(
                        ei.getObjectAttr(PermissionTracker[].class), len);
                PermissionTracker[] newAttr = new PermissionTracker[len];
                for (int i = 0; i < newAttr.length; i++) {
                    newAttr[i] = oldAttr[i].copy();
                }
                switch (methodName) {
                    case readAcquire:
                        newAttr[index] = newAttr[index].acquireR(ti);
                        break;
                    case writeAcquire:
                        newAttr[index] = newAttr[index].acquireW(ti);
                        break;
                    case readRelease:
                        newAttr[index] = newAttr[index].releaseR(ti);
                        break;
                    case writeRelease:
                        newAttr[index] = newAttr[index].releaseW(ti);
                        break;
                    default:
                        throw new PermissionError("Method not supported");
                }
                ei = ei.getModifiableInstance();
                if (ei.getObjectAttr(PermissionTracker[].class) == null) {
                    ei.addObjectAttr(newAttr);
                } else {
                    ei.replaceObjectAttr(oldAttr, newAttr);
                }
            }
        }
    }

    private void stateBlockTransitionOnArray(Object[] args, ThreadInfo ti,
            String methodName) {
        Object arg0 = args[0];
        Object arg1 = args[1];
        Object arg2 = args[2];
        if (arg0 instanceof ElementInfo && arg1 instanceof Integer
                && arg2 instanceof Integer) {
            ElementInfo ei = (ElementInfo) arg0;
            int start = (Integer) arg1;
            int end = (Integer) arg2;
//            System.out.println("Processing Array with id: " + ti.getId() +
//                    " and index: " + index);
            if (ei.isArray()) {
                ArrayFields arrayFields = ei.getArrayFields();
                int len = arrayFields.arrayLength();
                if (start > end || start < 0
                        || end < 0 || end >= len) {
                    throw new PermissionError("Invalid Array Indices "
                            + "within Permission Region");
                }
                PermissionTracker[] oldAttr = OPTSupport.withArrayDefault(
                        ei.getObjectAttr(PermissionTracker[].class), len);
                PermissionTracker[] newAttr = new PermissionTracker[len];
                for (int i = 0; i < newAttr.length; i++) {
                    newAttr[i] = oldAttr[i].copy();
                }
                for (int i = start; i <= end; i++) {
                    switch (methodName) {
                        case readAcquire:
                            newAttr[i] = newAttr[i].acquireR(ti);
                            break;
                        case writeAcquire:
                            newAttr[i] = newAttr[i].acquireW(ti);
                            break;
                        case readRelease:
                            newAttr[i] = newAttr[i].releaseR(ti);
                            break;
                        case writeRelease:
                            newAttr[i] = newAttr[i].releaseW(ti);
                            break;
                        default:
                            throw new PermissionError("Method not supported");
                    }
                }
                ei = ei.getModifiableInstance();
                if (ei.getObjectAttr(PermissionTracker[].class) == null) {
                    ei.addObjectAttr(newAttr);
                } else {
                    ei.replaceObjectAttr(oldAttr, newAttr);
                }
            }
        }
    }

    /*To Do
     Add error checking for cases where arg is returned as null value
     */
    @Override
    public void instructionExecuted(VM vm, ThreadInfo ti,
            Instruction nextInstruction, Instruction executedInstruction) {
        try {
            if (validInstruction(executedInstruction)) {
                JVMInvokeInstruction invokedInstruction
                        = (JVMInvokeInstruction) executedInstruction;
                if (validMethod(invokedInstruction, ti)) {
                    Object[] args = invokedInstruction.getArgumentValues(ti);
                    String methodName
                            = extractMethodName(invokedInstruction, ti);
                    switch (args.length) {
                        case 1:
                            stateTransitionOnObject(args, ti, methodName);
                            break;
                        case 2:
                            stateTransitionOnArray(args, ti, methodName);
                            break;
                        case 3:
                            stateBlockTransitionOnArray(args, ti, methodName);
                            break;
                        default:
                            System.err.println("Unreconized Method in "
                                    + "HjListener");
                            throw new PermissionError("Unsupported permission "
                                    + "method");
                    }
                }
            }
        } catch (PermissionError pe) {
//            System.out.println(pe.getLocalizedMessage());
            permissionExplanation = pe.getLocalizedMessage();
            permissionError = true;
        }
    }

    @Override
    public boolean check(Search search, VM vm) {
        return !permissionError;
    }

    @Override
    public String getErrorMessage() {
        return "Permission Error";
    }

    @Override
    public String getExplanation() {
        return permissionExplanation;
    }

    @Override
    public void objectShared(VM vm, ThreadInfo currentThread,
            ElementInfo sharedObject) {
        String elementType = sharedObject.getType();
        Instruction currentInstruction = currentThread.getPC();
        String methodName = currentThread.getPC().getMethodInfo().getBaseName();
        for (String ignore : ignored) {
            if (methodName.contains(ignore)) {
                return;
            }
        }
        System.out.println("Shared Object Detected: {");
        System.out.println("\t" + elementType);
        System.out.println("\t" + currentInstruction.getFileLocation());
        System.out.println("\t" + currentInstruction.getSourceLocation());
        System.out.println("}");
    }

}
