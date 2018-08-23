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
import gov.nasa.jpf.vm.choice.ThreadChoiceFromSet;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.ThreadList;
import gov.nasa.jpf.vm.VM;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
  // extend Thread. We may need to change Thread.join to Activity.join
  private static final String THREAD_START = "java.lang.Thread.join";
  private static final String THREAD_JOIN = "java.lang.Thread.join";
  private static final String START_ISOLATION = "";//TODO
  private static final String STOP_ISOLATION = "";//TODO
  private long startTime;

  // Tool that implements data race detection algorithm
  private final StructuredParallelRaceDetectorTool tool;

  // State stack for tool. Used to reset tool state when JPF backtracks
  private final Stack<Object> toolState = new Stack<>();

  // Subclass should extend this class and call super(conf, jpf, tool); with a specific tool
	public StructuredParallelRaceDetector(Config conf, JPF jpf, StructuredParallelRaceDetectorTool tool) {
    this.tool = tool;
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
    if (search.isNewState())
      toolState.push(tool.getImmutableState());
  }

  @Override
  public void stateBacktracked(Search search) {
    if (!toolState.isEmpty())
      tool.resetState(toolState.pop());
  }

  // VM interface
  @Override
  public void executeInstruction(VM vm, ThreadInfo ti, Instruction inst) {
    //scheduler for isolated
    //TODO verify whether check for ti.isFirstStepInsn() is needed
    if (inst instanceof JVMInvokeInstruction) {
      MethodInfo mi = ((JVMInvokeInstruction) inst).getInvokedMethod();
      if (isIsolatedMethod(mi)) {
        ChoiceGenerator<ThreadInfo> cg = getRunnableCG("ISOLATED", ti, vm);
        vm.getSystemState().setNextChoiceGenerator(cg);
      }
    } else if (inst instanceof ReadOrWriteInstruction)
      tool.handleAccess(ti.getId(), inst);
  }

  @Override
  public void methodEntered(VM vm, ThreadInfo ti, MethodInfo enteredMethod) {
    String mname = enteredMethod.getBaseName();
    if (mname.startsWith(STOP_ISOLATION)) {
      tool.handleRelease(ti.getId());
    } else if (mname.startsWith(THREAD_START)) {
      //TODO figure out how to get thread id of the started thread
      // If there is a way to get to the Thread object that the method
      // was called on then that would work
      
      tool.handleFork(ti.getId(), -1);
    }
  }

  @Override
  public void methodExited(VM vm, ThreadInfo ti, MethodInfo exitedMethod) {
    String mname = exitedMethod.getBaseName();
    if (mname.startsWith(START_ISOLATION)) {
      tool.handleAcquire(ti.getId());
    } else if (mname.startsWith(THREAD_JOIN)) {
      //TODO figure out how to get thread id of the future object joined with
      // If there is a way to get to the Thread object that the method
      // was called on then that would work
      
      tool.handleJoin(ti.getId(), -1);
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

	static ThreadInfo[] getTimeoutRunnables(VM vm, ApplicationContext appCtx) {
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

  boolean isIsolatedMethod(MethodInfo mi) {
    return mi.getBaseName().equals("edu.rice.hj.Module2.isolated");
  }
}
