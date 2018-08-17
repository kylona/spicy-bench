package common;

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

import java.util.Stack;

public class StructuredParallelRaceDetector extends PropertyListenerAdapter {
	private static final String RUNTIME = "hj.runtime.wsh";
	private static final String FUTURE = "hj.lang.Future";
	private static final String FUTURE_GET = "hj.lang.Future.get";
  private long startTime;

  private final StructuredParallelRaceDetectorTool tool;
  private final Stack<Object> toolState = new Stack<>();

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
      return;
    }

    if (inst instanceof ReadOrWriteInstruction) {
      tool.handleAccess(inst);
    }
  }

	@Override
  public void threadStarted(VM vm, ThreadInfo startedThread) {
    ThreadInfo parent = null; //TODO find parent
    tool.handleFork(parent, startedThread);
  }

	@Override
  public void objectCreated(VM vm, ThreadInfo ti, ElementInfo newObject) {
    //if (newObject.toString().startsWith(RUNTIME) || newObject.toString().startsWith(FUTURE)) {
    //  String newObjectID = getObjectId(newObject.toString());
    //  activityNode currentNode = (activityNode) currentNodes.get(ti);

    //  if (newObjectID.contains("Activity") || newObjectID.contains("Future")) {

    //    //create child node
    //    activityNode child = new activityNode(newObjectID + "-1");
    //    child.setDisplay_name(makeLabel(ti,newObjectID));
    //    graph.addVertex(child);

    //    if (!newObjectID.contains("Suspendable")) {
    //      addSpawnEdge(currentNodes.get(ti), child, graph);

    //      //create next node for parent task
    //      activityNode nextNode = createNextNode(ti);
    //      graph.addVertex(nextNode);
    //      addContinuationEdge(currentNode, nextNode, graph);

    //      //update current nodes map
    //      currentNodes.put(ti, nextNode);

    //      //update child's finish scope
    //      finishScope.put(newObjectID, finishBlocks.get(ti).peek());
    //    }

    //    updateTaskCount();
    //  } else if (newObjectID.contains("FinishScope")) {

    //    if (ti.getGlobalId() != 0) {
    //      finishNode fin = new finishNode(newObjectID, ti);
    //      fin.setDisplay_name(makeLabel(ti,newObjectID));
    //      graph.addVertex(fin);
    //      addContinuationEdge(currentNode, fin, graph);
    //      finishNode end = new finishNode(newObjectID + "-end", ti);
    //      end.setDisplay_name(makeLabel(ti,newObjectID+"-end") );
    //      graph.addVertex(end);

    //      //create next node for the task
    //      activityNode nextNode = createNextNode(ti);
    //      graph.addVertex(nextNode);
    //      addContinuationEdge(fin, nextNode, graph);

    //      currentNodes.put(ti, nextNode);

    //      //add finish node to the finish stack
    //      finishBlocks.get(ti).push(fin);
    //    } else {
    //      Node activity = null;
    //      Set<Node> nodes = graph.vertexSet();
    //      for(Node n : nodes) {
    //        if (n.id.startsWith("SuspendableActivity") && n.id.endsWith("-1")) {
    //          activity = n;
    //        }
    //      }

    //      //Master Finish Start
    //      finishNode fin = new finishNode(newObjectID, ti);
    //      fin.setDisplay_name(makeLabel(ti,newObjectID));
    //      graph.addVertex(fin);
    //      masterFin = fin;

    //      //Master Finish end
    //      finishNode fin_end = new finishNode(newObjectID+"-end", ti);
    //      fin_end.setDisplay_name(makeLabel(ti,newObjectID+"-end"));
    //      graph.addVertex(fin_end);
    //      masterFinEnd = fin_end;

    //      //add edge from finish start to suspendable activity
    //      addContinuationEdge(fin, activity, graph);

    //      //add finishScope of suspendable activity
    //      String susActID = activity.id.split("-")[0];
    //      finishScope.put(susActID, masterFin);
    //    }
    //  }
    //}
  }

	@Override
  public void methodEntered(VM vm, ThreadInfo ti, MethodInfo enteredMethod) {
    //TODO too.handleRelease if method is stopIsolation
    //TODO add thread to finish scope if method is startFinish
  }

	@Override
  public void methodExited(VM vm, ThreadInfo ti, MethodInfo enteredMethod) {
    //TODO tool.handleJoin if method is stopFinish or Future.get
    //TODO track finish scope to join all threads created in this block
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

  //Helper Methods
  String extractCalleeName(ThreadInfo ti) {
    String[] str = ti.getThisElementInfo().toString().split("\\.");
    return str[str.length - 1];
  }

  boolean isValidArrayInstruction(Instruction insn, ThreadInfo ti) {
    if (!(insn instanceof ArrayElementInstruction))
      return false;
    ArrayElementInstruction ainsn = (ArrayElementInstruction)insn;
    ElementInfo ei = ainsn.peekArrayElementInfo(ti);
    String className = ei.getClassInfo().getName();
    return !className.startsWith("[Ljava") && !className.startsWith("[Ledu");
  }

  boolean isIsolatedMethod(MethodInfo mi) {
    return mi.getBaseName().equals("edu.rice.hj.Module2.isolated");
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

  boolean isValidFieldInstruction(Instruction insn, VM vm) {
    return insn instanceof FieldInstruction && !isLibraryInstruction(insn);
  }

  String getObjectId(String str) {
      String[] s = str.split("\\.");
      return s[s.length - 1];
  }

  String extractObjectName(ElementInfo ei) {
      return getObjectId(ei.toString());
  }

  String extractThreadName(ThreadInfo ti) {
      return getObjectId(ti.getThreadObject().toString());
  }
}
