package cg;

import java.util.*;

import org.jgraph.graph.DefaultEdge;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;

import gov.nasa.jpf.jvm.bytecode.JVMInvokeInstruction;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.*;
import gov.nasa.jpf.vm.*;
import gov.nasa.jpf.vm.bytecode.*;
import gov.nasa.jpf.vm.choice.ThreadChoiceFromSet;

import static cg.Edges.*;
import static cg.Comp_Graph.*;

public class CGRaceDetector extends PropertyListenerAdapter {

	private String dir = null;
	private boolean on_the_fly = false;
	private boolean drd = true;

	private static final String[] invalidText = {"edu.rice", "hj.util", "hj.lang"};
	private static final String[] systemLibrary = {"java.util", "java.runtime", "java.lang", "null", "hj.runtime.wsh"};


	private String runtime = "hj.runtime.wsh";
	private String future = "hj.lang.Future";
	private String futureGet = "hj.lang.Future.get";

	private isolatedNode previousIsolatedNode = null;

	private Map<ThreadInfo, Node> currentNodes = new HashMap<ThreadInfo, Node>();
	private Map<ThreadInfo, Stack<finishNode>> finishBlocks = new HashMap<ThreadInfo, Stack<finishNode>>();

	private Map<String, Node> finishScope = new HashMap<String, Node>();

	private Map<String, Node> futureJoinNode = new HashMap<String, Node>();

	private static DirectedAcyclicGraph<Node, DefaultEdge> graph =
		new DirectedAcyclicGraph<Node, DefaultEdge>(DefaultEdge.class);

	private boolean race = false;
	private finishNode masterFinEnd = null;
	private finishNode masterFin = null;

	private Map<ThreadInfo, finishNode> currFinNode = new HashMap<ThreadInfo, finishNode>();

	public CGRaceDetector (Config conf, JPF jpf) {
		dir = conf.getString("Results_directory");
		String isol = conf.getString("Contains_isolated_sections");
		String otf = conf.getString("On_the_fly");
		String data_race_detect = conf.getString("Data_race_detection");
		if(otf.equalsIgnoreCase("true")){
			on_the_fly = true;
		}
		if(isol.equalsIgnoreCase("true")){
			on_the_fly = false;
		}
		if(data_race_detect.equalsIgnoreCase("false")){
			drd = false;
		}
	}

	@Override
		public void executeInstruction(VM vm, ThreadInfo currentThread,
				Instruction instructionToExecute) {

			//scheduler for isolated
			if(drd){
				if (instructionToExecute instanceof JVMInvokeInstruction) {
					MethodInfo mi = ((JVMInvokeInstruction) instructionToExecute).getInvokedMethod();
					String baseName = mi.getBaseName();
					if (isIsolatedMethod(baseName)) {
						ChoiceGenerator<ThreadInfo> cg
							= getRunnableCG("ISOLATED", currentThread, vm);
						vm.getSystemState().setNextChoiceGenerator(cg);
					}else{
						return;
					}
				}
			}

			Node n = currentNodes.get(currentThread);

			if(n != null){
				if(instructionToExecute instanceof ArrayElementInstruction){
					ElementInfo ei = ((ArrayElementInstruction) instructionToExecute).peekArrayElementInfo(currentThread);
					int idx = ((ArrayElementInstruction) instructionToExecute).peekIndex(currentThread);
					if(isValidArrayInstruction(instructionToExecute, currentThread)){
						ArrayElements ae = new ArrayElements(ei, idx);
						Boolean isReadInsn = ((ArrayElementInstruction) instructionToExecute).isRead();
						Boolean isIsolatedNode = false;
						if(n instanceof isolatedNode)
							isIsolatedNode = true;
						registerHeapAccessArray(n, ae, isReadInsn, isIsolatedNode);
					}
				}else if (instructionToExecute instanceof FieldInstruction) {
					ElementInfo ei = ((FieldInstruction) instructionToExecute).peekElementInfo(currentThread);
					FieldInfo fi = ((FieldInstruction) instructionToExecute).getFieldInfo();
					Elements var = new Elements(ei, fi);

					if(isValidFieldInstruction(instructionToExecute, vm)){
						Boolean isReadInsn = ((ReadOrWriteInstruction) instructionToExecute).isRead();
						//Boolean isIsolatedNode = (thread_holding_isolated == currentThread);
						Boolean isIsolatedNode = false;
						if(n instanceof isolatedNode)
							isIsolatedNode = true;
						registerHeapAccess(n, var, isReadInsn , isIsolatedNode);
					}
				}
			}
			createGraph(getGraph(vm, currentThread), dir, vm, "Line114");
		}

	@Override
		public void threadStarted(VM vm, ThreadInfo startedThread) {

			String thread_object = startedThread.getThreadObject().toString();
			String threadID = extractThreadName(startedThread);

			Node n = null;
			if (thread_object.startsWith(runtime) || thread_object.startsWith(future)) {
				n = searchGraph(threadID+"-1", getGraph(vm, startedThread));
				n.setThreadInfo(startedThread);
			}

			//update currentNodes map
			currentNodes.put(startedThread, n);

			//add finishStack
			Stack<finishNode> stk = new Stack<finishNode>();

			//add task's current finishscope to stack
			stk.push((finishNode) finishScope.get(threadID));
			finishBlocks.put(startedThread, stk);
			createGraph(getGraph(vm, startedThread), dir, vm, "Line138");

		}

	@Override
		public void threadTerminated(VM vm, ThreadInfo startedThread) {
			String thread_object = startedThread.getThreadObject().toString();
			String threadID = extractThreadName(startedThread);

			if (thread_object.startsWith(future)) {
				Node currentNode = currentNodes.get(startedThread);

				if(futureJoinNode.containsKey(threadID)){
					addJoinEdge(currentNode, futureJoinNode.get(threadID), getGraph(vm, startedThread));
				}else{
					futureJoinNode.put(threadID, currentNode);
				}
			}
			createGraph(getGraph(vm, startedThread), dir, vm, "Line156");
		}

	public String makeLabel(ThreadInfo currentThread, String objectID){
		String [] theadSplit = objectID.split("@");
		String output = theadSplit[0]+"T:"+getThreadName(theadSplit[1]);
		String trace;
		try{
			trace = currentThread.getStackTrace();
		}
		catch(Exception e){
			return objectID+"+eof";
		}
		String[] lines = trace.split("\\s+");
		String found="";

		//We need to search each line in the trace.
		for (String line : lines) {
			boolean invalid = false;
			if(line.equals("at")||line.equals("")||line.equals(" ")){
				invalid = true;
			}
			else{
				for(String bad: invalidText){
					if(line.contains(bad)){
						invalid = true;
					}
				}
				for(String bad: systemLibrary){
					if(line.contains(bad)){
						invalid = true;
					}
				}
			}

			if(!invalid){
				found =line;
				break;
			}
		}
		String[] outLine = found.split("\\(");
		String source = outLine[1].substring(0, outLine[1].length()-1);
		return output+"+"+source;
	}

	@Override
		public void objectCreated(VM vm, ThreadInfo currentThread,
				ElementInfo newObject) {

			if(newObject.toString().startsWith(runtime) || newObject.toString().startsWith(future)){
				String newObjectID = getObjectId(newObject.toString());
				activityNode currentNode = (activityNode) currentNodes.get(currentThread);

				if(newObjectID.contains("Activity") || newObjectID.contains("Future")){

					//create child node
					activityNode child = new activityNode(newObjectID + "-1");
					child.setDisplay_name(makeLabel(currentThread,newObjectID));
					getGraph(vm, currentThread).addVertex(child);

					if(!newObjectID.contains("Suspendable")){
						addSpawnEdge(currentNodes.get(currentThread), child, getGraph(vm, currentThread));

						//create next node for parent task
						activityNode nextNode = createNextNode(currentThread);
						getGraph(vm, currentThread).addVertex(nextNode);
						addContinuationEdge(currentNode, nextNode, getGraph(vm, currentThread));

						//update current nodes map
						currentNodes.put(currentThread, nextNode);

						//update child's finish scope
						finishScope.put(newObjectID, finishBlocks.get(currentThread).peek());
					}

				}else if(newObjectID.contains("FinishScope")){

					if(currentThread.getGlobalId() != 0){
						finishNode fin = new finishNode(newObjectID, currentThread);
						fin.setDisplay_name(makeLabel(currentThread,newObjectID));
						getGraph(vm, currentThread).addVertex(fin);
						addContinuationEdge(currentNode, fin, getGraph(vm, currentThread));

						//create next node for the task
						activityNode nextNode = createNextNode(currentThread);
						getGraph(vm, currentThread).addVertex(nextNode);
						addContinuationEdge(fin, nextNode, getGraph(vm, currentThread));

						currentNodes.put(currentThread, nextNode);

						//add finish node to the finish stack
						finishBlocks.get(currentThread).push(fin);
					}else{
						Node activity = null;
						Set<Node> nodes = getGraph(vm, currentThread).vertexSet();
						for(Node n : nodes){
							if(n.id.startsWith("SuspendableActivity") && n.id.endsWith("-1")){
								activity = n;
							}
						}

						//Master Finish Start
						finishNode fin = new finishNode(newObjectID, currentThread);
						fin.setDisplay_name(makeLabel(currentThread,newObjectID));
						getGraph(vm, currentThread).addVertex(fin);
						masterFin = fin;

						//Master Finish end
						finishNode fin_end = new finishNode(newObjectID+"-end", currentThread);
						fin_end.setDisplay_name(makeLabel(currentThread,newObjectID+"-end"));
						getGraph(vm, currentThread).addVertex(fin_end);
						masterFinEnd = fin_end;

						//add edge from finish start to suspendable activity
						addContinuationEdge(fin, activity, getGraph(vm, currentThread));

						//add finishScope of suspendable activity
						String susActID = activity.id.split("-")[0];
						finishScope.put(susActID, masterFin);
					}
				}
			}
			createGraph(getGraph(vm, currentThread), dir, vm, "Line278");
		}

	@Override
		public void objectReleased(VM vm, ThreadInfo currentThread,
				ElementInfo releasedObject) {

			if (releasedObject.toString().startsWith(runtime)) {

				String objectName = extractObjectName(releasedObject);
				if (objectName.startsWith("SuspendableActivity")) {
					//add edge to master fin end
					Node activity = currentNodes.get(currentThread);
					addContinuationEdge(activity, masterFinEnd, getGraph(vm, currentThread));
					createGraph(getGraph(vm, currentThread), dir, vm, "Line292");
					if(drd){
						race = analyzeFinishBlock(getGraph(vm, currentThread), masterFin.id, on_the_fly);
					}
				}
			}
			createGraph(getGraph(vm, currentThread), dir, vm, "Line298");
		}

	@Override
		public void methodEntered(VM vm, ThreadInfo currentThread,
				MethodInfo enteredMethod) {

			String methodName = extractMethodName(enteredMethod);
			if (methodName.startsWith("startIsolation")) {
				activityNode currentActivity = (activityNode) currentNodes.get(currentThread);
				String [] s = currentActivity.id.split("-");
				int next_num = Integer.parseInt(s[1]) + 1;

				isolatedNode isolNode = new isolatedNode("Isolated_" + s[0] + "-" + next_num, currentThread);
				isolNode.setDisplay_name(makeLabel(currentThread,"Isolated@" + next_num));
				currentNodes.put(currentThread, isolNode);

				getGraph(vm, currentThread).addVertex(isolNode);
				if (getGraph(vm, currentThread).containsVertex(currentActivity)) {
					addContinuationEdge(currentActivity, isolNode, getGraph(vm, currentThread));
				}
				else {
					System.out.println("Skipped adding edge");
				}


				if(previousIsolatedNode != null){
					addIsolatedEdge(previousIsolatedNode, isolNode, getGraph(vm, currentThread));
				}
				previousIsolatedNode = isolNode;

			} else if (methodName.startsWith("stopIsolation")) {

				Node isolatedNode = currentNodes.get(currentThread);
				activityNode nextNode = createNextNode(currentThread);
				getGraph(vm, currentThread).addVertex(nextNode);
				addContinuationEdge(isolatedNode, nextNode, getGraph(vm, currentThread));

				currentNodes.put(currentThread, nextNode);

			} else if (methodName.startsWith("stopFinish")) {
				String threadID = extractThreadName(currentThread);

				if(finishBlocks.get(currentThread).size() == 1){
					if(!threadID.contains("Suspendable")){
						Node currentNode = currentNodes.get(currentThread);
						Node fin = finishScope.get(threadID);
						String finishJoin = fin.id;
						Node finishJoinNode = searchGraph(finishJoin+"-end", getGraph(vm, currentThread));
						addJoinEdge(currentNode, finishJoinNode, getGraph(vm, currentThread));
					}
				}else if(finishBlocks.get(currentThread).size() > 1){
					createFinEndNode(currentThread, vm);
				}
			} else if(enteredMethod.getBaseName().equals(futureGet)){

				String futureThreadName = extractCalleeName(currentThread);

				Node oldActNode = currentNodes.get(currentThread);
				Node newActNode = createNextNode(currentThread);
				getGraph(vm, currentThread).addVertex(newActNode);
				addContinuationEdge(oldActNode, newActNode, getGraph(vm, currentThread));

				currentNodes.put(currentThread, newActNode);

				if(!futureJoinNode.containsKey(futureThreadName)){
					futureJoinNode.put(futureThreadName, newActNode);
				}else{
					addJoinEdge(futureJoinNode.get(futureThreadName), newActNode, getGraph(vm, currentThread));
				}

			}
			createGraph(getGraph(vm, currentThread), dir, vm, "Line366");
		}

	@Override
		public void methodExited(VM vm, ThreadInfo currentThread, MethodInfo enteredMethod) {

			if(enteredMethod.getName().contains("stopFinish")){
				if(on_the_fly && drd){
					race = analyzeFinishBlock(getGraph(vm, currentThread), finishBlocks.get(currentThread).pop().id, on_the_fly);
				}
			}
		}

	@Override
		public String getErrorMessage () {
			return "Data Race detected";
		}

	@Override
		public boolean check(Search search, VM vm) {
			createGraph(graph, dir, vm, "Line386");

			return (!race);
		}

	private activityNode createNextNode(ThreadInfo currentThread){

		String threadID = extractThreadName(currentThread);
		activityNode currentNode = (activityNode) currentNodes.get(currentThread);
		int next_num = Integer.parseInt(currentNode.id.split("-")[1]) + 1;
		activityNode nextNode = new activityNode(threadID + "-" + next_num, currentThread);
		nextNode.setDisplay_name(makeLabel(currentThread,threadID));
		return nextNode;
	}

	private void createFinEndNode(ThreadInfo currentThread, VM vm){

		activityNode currentNode = (activityNode) currentNodes.get(currentThread);

		finishNode start = finishBlocks.get(currentThread).pop();

		finishNode end = new finishNode(start.id + "-end", currentThread);
		end.setDisplay_name(makeLabel(currentThread,start.id+"-end") );
		getGraph(vm, currentThread).addVertex(end);
		addContinuationEdge(currentNode, end, getGraph(vm, currentThread));

		currFinNode.put(currentThread, start);

		//create next node for the task
		activityNode nextNode = createNextNode(currentThread);
		getGraph(vm, currentThread).addVertex(nextNode);

		currentNodes.put(currentThread, nextNode);
		addContinuationEdge(end, nextNode, getGraph(vm, currentThread));

	}

	static ThreadInfo[] getTimeoutRunnables(VM vm, ApplicationContext appCtx) {
		ThreadList tlist = vm.getThreadList();
		if (tlist.hasProcessTimeoutRunnables(appCtx)) {
			return tlist.getProcessTimeoutRunnables(appCtx);
		} else {
			return tlist.getTimeoutRunnables();
		}
	}

	static ChoiceGenerator<ThreadInfo> getRunnableCG(String id, ThreadInfo tiCurrent, VM vm) {
		ThreadInfo[] timeoutRunnables
			= getTimeoutRunnables(vm, tiCurrent.getApplicationContext());
		if (timeoutRunnables.length == 0) {
			return null;
		} else if (timeoutRunnables.length == 1
				&& timeoutRunnables[0] == tiCurrent) {
			return null;
		} else {
			return new SeperateGraphChoiceGenerator(id, timeoutRunnables, true, getGraph(vm, tiCurrent));
		}
	}
	/** TODO have tool analize all of the created graphs */
	@Override
		public void searchFinished(Search search) {
			createGraph(graph, dir, search.getVM(), "Line447");
			if(drd){
				race = analyzeFinishBlock(graph, masterFin.id, on_the_fly);
			}
			check(search, search.getVM());
		}


		static DirectedAcyclicGraph<Node, DefaultEdge> getGraph(VM vm, ThreadInfo ti) {
			if (SeperateGraphChoiceGenerator.class.isInstance(vm.getChoiceGenerator())) {
				SeperateGraphChoiceGenerator cg = (SeperateGraphChoiceGenerator) vm.getChoiceGenerator();
				graph = cg.getGraph(vm.getCurrentThread());
				return graph;
			}
			else {
				return graph;
			}
		}
}
