package extensions.fasttrack;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.jvm.bytecode.JVMInvokeInstruction;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.vm.ApplicationContext;
import gov.nasa.jpf.vm.bytecode.ArrayElementInstruction;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;
//import gov.nasa.jpf.vm.choice.ThreadChoiceFromSet;

public class FastTrackIsolatedBlockListener extends PropertyListenerAdapter {
	private static final String[] INVALID_TEXT = {"edu.rice", "hj.util", "hj.lang"};
	private static final String[] SYSTEM_LIBRARY = {"java.util", "java.runtime", "java.lang", "null", "hj.runtime.wsh"};
	private static final String RUNTIME = "hj.runtime.wsh";
	private static final String FUTURE = "hj.lang.Future";
	private static final String FUTURE_GET = "hj.lang.Future.get";
  private long startTime;

  private FastTrackTool tool = new FastTrackTool();

	public FastTrackIsolatedBlockListener(Config conf, JPF jpf) {
    startTime = System.currentTimeMilliseconds();
    tool.resetToSnapshot(INITIAL_STATE);
	}

	@Override
  public void executeInstruction(VM vm, ThreadInfo ti, Instruction inst) {
    //scheduler for isolated
    if (inst instanceof JVMInvokeInstruction) {
      MethodInfo mi = ((JVMInvokeInstruction) inst).getInvokedMethod();
      String baseName = mi.getBaseName();
      if (isIsolatedMethod(baseName)) {
        ChoiceGenerator<ThreadInfo> cg = getRunnableCG("ISOLATED", ti, vm);
        vm.getSystemState().setNextChoiceGenerator(cg);
      } else {
        return;
      }
    }

    if (inst instanceof ArrayElementInstruction) {
      if (isValidArrayInstruction(inst, ti)) {
        ArrayElements ae = new ArrayElements(ei, idx, filePos);
        Boolean isReadInsn = ((ArrayElementInstruction) inst).isRead();
        Boolean isIsolatedNode = false;
        if (n instanceof isolatedNode)
          isIsolatedNode = true;
      }
    } else if (inst instanceof FieldInstruction) {
      ElementInfo ei = ((FieldInstruction) inst).peekElementInfo(ti);
      FieldInfo fi = ((FieldInstruction) inst).getFieldInfo();
      String filePos = inst.getFilePos();
      Elements var = new Elements(ei, fi, filePos);

      if (isValidFieldInstruction(inst, vm)) {
        Boolean isReadInsn = ((ReadOrWriteInstruction) inst).isRead();
        //Boolean isIsolatedNode = (thread_holding_isolated == ti);
        Boolean isIsolatedNode = false;
        if (n instanceof isolatedNode)
          isIsolatedNode = true;
        registerHeapAccess(n, var, isReadInsn , isIsolatedNode);
      }
    }
  }

	@Override
		public void threadStarted(VM vm, ThreadInfo startedThread) {

			String thread_object = startedThread.getThreadObject().toString();
			String threadID = extractThreadName(startedThread);

			Node n = null;
			if (thread_object.startsWith(runtime) || thread_object.startsWith(future)) {
				n = searchGraph(threadID+"-1", graph);
				n.setThreadInfo(startedThread);
			}

			//update currentNodes map
			currentNodes.put(startedThread, n);

			//add finishStack
			Stack<finishNode> stk = new Stack<finishNode>();

			//add task's current finishscope to stack
			stk.push((finishNode) finishScope.get(threadID));
			finishBlocks.put(startedThread, stk);
		}

	@Override
		public void threadTerminated(VM vm, ThreadInfo startedThread) {
			String thread_object = startedThread.getThreadObject().toString();
			String threadID = extractThreadName(startedThread);

			if (thread_object.startsWith(future)) {
				Node currentNode = currentNodes.get(startedThread);

				if (futureJoinNode.containsKey(threadID)) {
					addJoinEdge(currentNode, futureJoinNode.get(threadID), graph);
				} else {
					futureJoinNode.put(threadID, currentNode);
				}
			}
			updateTaskCount();
		}

	public String makeLabel(ThreadInfo ti, String objectID) {
		String [] theadSplit = objectID.split("@");
		String output = theadSplit[0]+"T:"+getThreadName(theadSplit[1]);
		String trace;
		try {
			trace = ti.getStackTrace();
		}
		catch(Exception e) {
			return objectID+"+eof";
		}
		String[] lines = trace.split("\\s+");
		String found="";

		//We need to search each line in the trace.
		for (String line : lines) {
			boolean invalid = false;
			if (line.equals("at")||line.equals("")||line.equals(" ")) {
				invalid = true;
			}
			else {
				for(String bad: invalidText) {
					if (line.contains(bad)) {
						invalid = true;
					}
				}
				for(String bad: systemLibrary) {
					if (line.contains(bad)) {
						invalid = true;
					}
				}
			}

			if (!invalid) {
				found =line;
				break;
			}
		}
		String[] outLine = found.split("\\(");
		String source = outLine[1].substring(0, outLine[1].length()-1);
		return output+"+"+source;
	}

	@Override
		public void objectCreated(VM vm, ThreadInfo ti,
				ElementInfo newObject) {

			if (newObject.toString().startsWith(runtime) || newObject.toString().startsWith(future)) {
				String newObjectID = getObjectId(newObject.toString());
				activityNode currentNode = (activityNode) currentNodes.get(ti);

				if (newObjectID.contains("Activity") || newObjectID.contains("Future")) {

					//create child node
					activityNode child = new activityNode(newObjectID + "-1");
					child.setDisplay_name(makeLabel(ti,newObjectID));
					graph.addVertex(child);

					if (!newObjectID.contains("Suspendable")) {
						addSpawnEdge(currentNodes.get(ti), child, graph);

						//create next node for parent task
						activityNode nextNode = createNextNode(ti);
						graph.addVertex(nextNode);
						addContinuationEdge(currentNode, nextNode, graph);

						//update current nodes map
						currentNodes.put(ti, nextNode);

						//update child's finish scope
						finishScope.put(newObjectID, finishBlocks.get(ti).peek());
					}

					updateTaskCount();
				} else if (newObjectID.contains("FinishScope")) {

					if (ti.getGlobalId() != 0) {
						finishNode fin = new finishNode(newObjectID, ti);
						fin.setDisplay_name(makeLabel(ti,newObjectID));
						graph.addVertex(fin);
						addContinuationEdge(currentNode, fin, graph);
						finishNode end = new finishNode(newObjectID + "-end", ti);
						end.setDisplay_name(makeLabel(ti,newObjectID+"-end") );
						graph.addVertex(end);

						//create next node for the task
						activityNode nextNode = createNextNode(ti);
						graph.addVertex(nextNode);
						addContinuationEdge(fin, nextNode, graph);

						currentNodes.put(ti, nextNode);

						//add finish node to the finish stack
						finishBlocks.get(ti).push(fin);
					} else {
						Node activity = null;
						Set<Node> nodes = graph.vertexSet();
						for(Node n : nodes) {
							if (n.id.startsWith("SuspendableActivity") && n.id.endsWith("-1")) {
								activity = n;
							}
						}

						//Master Finish Start
						finishNode fin = new finishNode(newObjectID, ti);
						fin.setDisplay_name(makeLabel(ti,newObjectID));
						graph.addVertex(fin);
						masterFin = fin;

						//Master Finish end
						finishNode fin_end = new finishNode(newObjectID+"-end", ti);
						fin_end.setDisplay_name(makeLabel(ti,newObjectID+"-end"));
						graph.addVertex(fin_end);
						masterFinEnd = fin_end;

						//add edge from finish start to suspendable activity
						addContinuationEdge(fin, activity, graph);

						//add finishScope of suspendable activity
						String susActID = activity.id.split("-")[0];
						finishScope.put(susActID, masterFin);
					}
				}
			}
		}

	@Override
		public void objectReleased(VM vm, ThreadInfo ti,
				ElementInfo releasedObject) {

			if (releasedObject.toString().startsWith(runtime)) {

				String objectName = extractObjectName(releasedObject);
				if (objectName.startsWith("SuspendableActivity")) {
					//add edge to master fin end
					Node activity = currentNodes.get(ti);
					addContinuationEdge(activity, masterFinEnd, graph);
					System.out.println("Checking Graph " + createGraph(graph, dir, vm));
                    long time = System.currentTimeMillis();
                    System.out.println("GraphSize: " + graph.vertexSet().size());
					if (drd) {
						race = analyzeFinishBlock(graph, masterFin.id, on_the_fly);
					}
                    if (zip_drd) {
                        race = ZipperAnalyzer.analyze(graph,orderedIsolatedNodes, numberOfAsyncEdges);
                    }
                    timeAnalyzing += System.currentTimeMillis() - time;
				}
			}
		}

	@Override
		public void methodEntered(VM vm, ThreadInfo ti,
				MethodInfo enteredMethod) {

			String methodName = extractMethodName(enteredMethod);
			if (methodName.startsWith("startIsolation")) {
				activityNode currentActivity = (activityNode) currentNodes.get(ti);
				String [] s = currentActivity.id.split("-");
				int next_num = Integer.parseInt(s[1]) + 1;

				isolatedNode isolNode = new isolatedNode("Isolated_" + s[0] + "-" + next_num, ti);
				isolNode.setDisplay_name(makeLabel(ti,"Isolated@" + next_num));
				graph.addVertex(isolNode);

				currentNodes.put(ti, isolNode);

				addContinuationEdge(currentActivity, isolNode, graph);

				if (previousIsolatedNode != null) {
					addIsolatedEdge(previousIsolatedNode, isolNode, graph);
				}
				previousIsolatedNode = isolNode;
                orderedIsolatedNodes.add(isolNode);

			} else if (methodName.startsWith("stopIsolation")) {

				Node isolatedNode = currentNodes.get(ti);
				activityNode nextNode = createNextNode(ti);
				graph.addVertex(nextNode);
				addContinuationEdge(isolatedNode, nextNode, graph);

				currentNodes.put(ti, nextNode);

			} else if (methodName.startsWith("stopFinish")) {
				String threadID = extractThreadName(ti);

				if (finishBlocks.get(ti).size() == 1) {
					if (!threadID.contains("Suspendable")) {
						Node currentNode = currentNodes.get(ti);
						Node fin = finishScope.get(threadID);
						String finishJoin = fin.id;
						Node finishJoinNode = searchGraph(finishJoin+"-end", graph);
						addJoinEdge(currentNode, finishJoinNode, graph);
					}
				} else if (finishBlocks.get(ti).size() > 1) {
					createFinEndNode(ti);
				}
			} else if (enteredMethod.getBaseName().equals(futureGet)) {

				String futureThreadName = extractCalleeName(ti);

				Node oldActNode = currentNodes.get(ti);
				Node newActNode = createNextNode(ti);
				graph.addVertex(newActNode);
				addContinuationEdge(oldActNode, newActNode, graph);

				currentNodes.put(ti, newActNode);

				if (!futureJoinNode.containsKey(futureThreadName)) {
					futureJoinNode.put(futureThreadName, newActNode);
				} else {
					addJoinEdge(futureJoinNode.get(futureThreadName), newActNode, graph);
				}

				updateTaskCount();
			}
		}

	@Override
  public void methodExited(VM vm, ThreadInfo ti, MethodInfo enteredMethod) {

    if (enteredMethod.getName().contains("stopFinish") && on_the_fly &&
        drd) {
              long time = System.currentTimeMillis();
      race = analyzeFinishBlock(graph,
          finishBlocks.get(ti).pop().id, on_the_fly);
              timeAnalyzing += System.currentTimeMillis() - time;
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
		ThreadInfo[] timeoutRunnables
			= getTimeoutRunnables(vm, tiCurrent.getApplicationContext());
		if (timeoutRunnables.length == 0) {
			return null;
		} else if (timeoutRunnables.length == 1
				&& timeoutRunnables[0] == tiCurrent) {
			return null;
		} else {
			return new IsolateChoiceGenerator(this, id, timeoutRunnables, true, dir, vm);
		}
	}

	@Override
  public void searchFinished(Search search) {
    System.out.println("Time Analyzing: " + (System.currentTimeMillis() - startTime));
  }
}
