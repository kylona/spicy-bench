package extensions.util;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.jvm.bytecode.JVMInvokeInstruction;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.vm.ApplicationContext;
import gov.nasa.jpf.vm.bytecode.ArrayElementInstruction;
import gov.nasa.jpf.vm.bytecode.FieldInstruction;
import gov.nasa.jpf.vm.bytecode.ReadOrWriteInstruction;
import gov.nasa.jpf.vm.bytecode.StaticFieldInstruction;
import gov.nasa.jpf.vm.choice.ThreadChoiceFromSet;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.ThreadList;
import gov.nasa.jpf.vm.VM;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Data race detector for structured parallel programs.
 * This class makes JPF enumerate all thread schedules over isolated
 * regions in a Habanero Java program. It takes a tool as an input and
 * the tool is responsible for implementing an algorithm that can detect
 * data race in the program. This class passes information about each
 * read/write and synchronization event to the tool and also tells the
 * tool when to record a snapshot of its state and when to reset its state
 * when JPF backtracks.
 */
public class StructuredParallelRaceDetector extends PropertyListenerAdapter {
  // Method names
  // I think that we only need to trigger fork and join handlers on
  // Thread.start and Thread.join because Habanero Java classes just
  // extend Thread.
  private static final String THREAD_START = "java.lang.Thread.start";
  private static final String THREAD_JOIN = "java.lang.Thread.join";
  private static final String ISOLATED = "edu.rice.hj.Module2.isolated";
  private static final String START_ISOLATION = "hj.lang.Runtime.startIsolation";
  private static final String STOP_ISOLATION = "hj.lang.Runtime.stopIsolation";
  private static final String FUTURE_GET = "hj.lang.Future.get";
  private long startTime;
  boolean debug = false;
  int prevStateId = -1;

  // Tool that implements data race detection algorithm
  protected final StructuredParallelRaceDetectorTool tool;

  // State stack for tool. Used to reset tool state when JPF backtracks
  private final Stack<Object> toolState = new Stack<>();

  // Map of thread refs to dense thread ids
  private final Map<Integer, Integer> tids = new HashMap<>();

  // Initial tool state for reset
  private final Object initialState;

  // Subclass should extend this class and call super(conf, jpf, tool); with a specific tool
	public StructuredParallelRaceDetector(Config conf, JPF jpf, StructuredParallelRaceDetectorTool tool) {
    debug = conf.getString("debug").equalsIgnoreCase("true");
    this.tool = tool;
    initialState = this.tool.getImmutableState();
	}

  @Override
  public void reset() {
    tool.resetState(initialState);
  }

  void debug(String message) {
    if (debug) {
      System.out.println(message);
      System.out.println(tool.getImmutableState().toString());
    }
  }

  // Search interface
  @Override
  public void searchStarted(Search search) {
    super.searchStarted(search);
    startTime = System.currentTimeMillis();
  }

  @Override
  public void searchFinished(Search search) {
    System.out.println("Time Analyzing: " + (System.currentTimeMillis() - startTime));
  }

  @Override
  public void stateAdvanced(Search search) {
    debug("Before save state advancing from " + prevStateId + " to " + search.getStateId());
    toolState.push(tool.getImmutableState());
    prevStateId = search.getStateId();
  }

  @Override
  public void stateBacktracked(Search search) {
    debug("Before reset state backtracking from " + prevStateId + " to " + search.getStateId());
    tool.resetState(toolState.pop());
    debug("After reset state backtracking from " + prevStateId + " to " + search.getStateId());
    prevStateId = search.getStateId();
  }

  // VM interface
  @Override
  public void executeInstruction(VM vm, ThreadInfo ti, Instruction inst) {
    //scheduler for isolated
    String label;
    if (inst instanceof JVMInvokeInstruction) {
      MethodInfo mi = ((JVMInvokeInstruction) inst).getInvokedMethod();
      String mname = mi.getBaseName();
      if (mname.equals(ISOLATED)) {
        ChoiceGenerator<ThreadInfo> cg = getRunnableCG("ISOLATED", ti, vm);
        vm.getSystemState().setNextChoiceGenerator(cg);
      }
    } else if ((label = getUniqueLabel(inst, ti)) != null) {
      ReadOrWriteInstruction rw = (ReadOrWriteInstruction)inst;
      int tid = getTid(ti.getThreadObjectRef());
      if (rw.isRead()) {
        debug("Before read " + label + " on " + tid);
        tool.handleRead(tid, label);
        debug("After read " + label + " on " + tid);
      } else {
        debug("Before write " + label + " on " + tid);
        tool.handleWrite(tid, label);
        debug("After write " + label + " on " + tid);
      }
    }
  }

  @Override
  public void methodEntered(VM vm, ThreadInfo ti, MethodInfo enteredMethod) {
    String mname = enteredMethod.getBaseName();
    int tid = getTid(ti.getThreadObjectRef());
    if (mname.startsWith(STOP_ISOLATION)) {
      debug("Before release on " + tid);
      tool.handleRelease(tid);
      debug("After release on " + tid);
    } else if (mname.startsWith(THREAD_START)) {
      int child = getTid(ti.getThis());
      debug("Before fork of " + child + " on " + tid);
      tool.handleFork(tid, child);
      debug("After fork of " + child + " on " + tid);
    }
  }

  @Override
  public void methodExited(VM vm, ThreadInfo ti, MethodInfo exitedMethod) {
    String mname = exitedMethod.getBaseName();
    int tid = getTid(ti.getThreadObjectRef());
    if (mname.startsWith(START_ISOLATION)) {
      debug("Before acquire on " + tid);
      tool.handleAcquire(tid);
      debug("After acquire on " + tid);
    } else if (mname.startsWith(THREAD_JOIN) || mname.startsWith(FUTURE_GET)) {
      int child = getTid(ti.getThis());
      debug("Before join of " + child + " on " + tid);
      tool.handleJoin(tid, child);
      debug("After join of " + child + " on " + tid);
    }
  }

  @Override
  public String getErrorMessage () {
    return tool.error();
  }

  @Override
  public boolean check(Search search, VM vm) {
    return !tool.race();
  }

  // Helper methods
	ThreadInfo[] getTimeoutRunnables(VM vm, ApplicationContext appCtx) {
		ThreadList tlist = vm.getThreadList();
		if (tlist.hasProcessTimeoutRunnables(appCtx)) {
			return tlist.getProcessTimeoutRunnables(appCtx);
		} else {
			return tlist.getTimeoutRunnables();
		}
	}

	ChoiceGenerator<ThreadInfo> getRunnableCG(String id, ThreadInfo tiCurrent, VM vm) {
		ThreadInfo[] timeoutRunnables = getTimeoutRunnables(vm, tiCurrent.getApplicationContext());
		if (timeoutRunnables.length == 0) {
			return null;
		} else if (timeoutRunnables.length == 1 && timeoutRunnables[0] == tiCurrent) {
			return null;
		} else {
			return new ThreadChoiceFromSet(id, timeoutRunnables, true);
		}
	}

  int getTid(int threadRef) {
    if (!tids.containsKey(threadRef)) {
      tids.put(threadRef, tids.size());
    }
    return tids.get(threadRef);
  }

  String getUniqueLabel(Instruction insn, ThreadInfo ti) {
    if (!isValidArrayElementInstruction(insn, ti) && !isValidFieldInstruction(insn)) {
      return null;
    }
    if (insn instanceof ArrayElementInstruction) {
      ArrayElementInstruction ainsn = ((ArrayElementInstruction)insn);
      int objRef = ainsn.peekArrayElementInfo(ti).getObjectRef();
      int index = ainsn.peekIndex(ti);
      return "array@" + objRef + "[" + index + "]";
    } else if (insn instanceof FieldInstruction) {
      FieldInstruction finsn = ((FieldInstruction)insn);
      ElementInfo ei = finsn.getElementInfo(ti);
      return finsn.getId(ei);
    }
    return null;
  }

  boolean isValidArrayElementInstruction(Instruction inst, ThreadInfo ti) {
    if (!(inst instanceof ArrayElementInstruction)) {
      return false;
    }
    ElementInfo ei = ((ArrayElementInstruction) inst).peekArrayElementInfo(ti);
    String klass = ei.getClassInfo().getName();
    if (!klass.startsWith("[Ljava") && !klass.startsWith("[Ledu")) {
        return true;
    }
    return false;
  }

  boolean isValidFieldInstruction(Instruction insn) {
    return insn instanceof FieldInstruction && !isLibraryInstruction(insn);
  }

  boolean isLibraryInstruction(Instruction insn) {
    String className = ((FieldInstruction) insn).getClassName();
    return className.startsWith("java") || className.startsWith("hj") ||
        (className.startsWith("edu") &&
         !(className.startsWith("edu.rice.hj.api.HjActor") ||
           className.startsWith("edu.rice.hj.api.HjDataDrivenFuture") ||
           className.startsWith("edu.rice.hj.api.HjFinishAccumulator") ||
           className.startsWith("edu.rice.hj.api.HjFuture") ||
           className.startsWith("edu.rice.hj.api.HjLambda") ||
           className.startsWith("edu.rice.hj.api.HjRunnable") ||
           className.startsWith("edu.rice.hj.api.HjSuspendable") ||
           className.startsWith("edu.rice.hj.api.HjSuspendingCallable")));
  }
}
