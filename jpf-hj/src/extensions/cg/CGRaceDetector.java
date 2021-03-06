package extensions.cg;

import java.util.*;

import org.jgraph.graph.DefaultEdge;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;

import gov.nasa.jpf.jvm.bytecode.JVMInvokeInstruction;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.*;
import gov.nasa.jpf.vm.*;
import gov.nasa.jpf.vm.bytecode.*;
import gov.nasa.jpf.vm.choice.ThreadChoiceFromSet;

import static extensions.cg.Edges.*;
import static extensions.cg.Comp_Graph.*;


public class CGRaceDetector extends PropertyListenerAdapter {

	private static String dir = null;
	private static boolean on_the_fly = false;
	private static boolean drd = false;
    public static boolean zip_drd = true;

	private static final String[] invalidText = {"edu.rice", "hj.util", "hj.lang"};
	private static final String[] systemLibrary = {"java.util", "java.runtime", "java.lang", "null", "hj.runtime.wsh"};


	private static final String runtime = "hj.runtime.wsh";
	private static final String future = "hj.lang.Future";
	private static final String futureGet = "hj.lang.Future.get";

    static long timeAnalyzing = 0;

	isolatedNode previousIsolatedNode = null;
    List<isolatedNode> orderedIsolatedNodes = new ArrayList<isolatedNode>();

	Map<ThreadInfo, Node> currentNodes = new HashMap<ThreadInfo, Node>();
	Map<ThreadInfo, Stack<finishNode>> finishBlocks = new HashMap<ThreadInfo, Stack<finishNode>>();

	Map<String, Node> finishScope = new HashMap<String, Node>();

	Map<String, Node> futureJoinNode = new HashMap<String, Node>();

	static DirectedAcyclicGraph<Node, DefaultEdge> graph =
		new DirectedAcyclicGraph<Node, DefaultEdge>(DefaultEdge.class);

	boolean race = false;
	finishNode masterFinEnd = null;
	finishNode masterFin = null;

	Map<ThreadInfo, finishNode> currFinNode = new HashMap<ThreadInfo, finishNode>();


	static int numberOfAsyncEdges = 1; //start at one so main thread gets counted
	static int numberOfFutureEdges = 0;
	static int numberOfJoinEdges = 0;
	static int maxNumberOfTasks = 0;
	static int maxNumberOfActiveTasks = 0;

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
			if(drd || zip_drd){
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
					String filePos = instructionToExecute.getFilePos();
					if(isValidArrayInstruction(instructionToExecute, currentThread)){
						ArrayElements ae = new ArrayElements(ei, idx, filePos);
						Boolean isReadInsn = ((ArrayElementInstruction) instructionToExecute).isRead();
						Boolean isIsolatedNode = false;
						if(n instanceof isolatedNode)
							isIsolatedNode = true;
						registerHeapAccessArray(n, ae, isReadInsn, isIsolatedNode);
					}
				}else if (instructionToExecute instanceof FieldInstruction) {
					ElementInfo ei = ((FieldInstruction) instructionToExecute).peekElementInfo(currentThread);
					FieldInfo fi = ((FieldInstruction) instructionToExecute).getFieldInfo();
					String filePos = instructionToExecute.getFilePos();
					Elements var = new Elements(ei, fi, filePos);

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

				if(futureJoinNode.containsKey(threadID)){
					addJoinEdge(currentNode, futureJoinNode.get(threadID), graph);
				}else{
					futureJoinNode.put(threadID, currentNode);
				}
			}
			updateTaskCount();
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
					graph.addVertex(child);

					if(!newObjectID.contains("Suspendable")){
                        if(newObjectID.contains("Future")) addFutureEdge(currentNodes.get(currentThread), child, graph);
                        else addSpawnEdge(currentNodes.get(currentThread), child, graph);

						//create next node for parent task
						activityNode nextNode = createNextNode(currentThread);
						graph.addVertex(nextNode);
						addContinuationEdge(currentNode, nextNode, graph);

						//update current nodes map
						currentNodes.put(currentThread, nextNode);

						//update child's finish scope
						finishScope.put(newObjectID, finishBlocks.get(currentThread).peek());
					}

					updateTaskCount();
				}else if(newObjectID.contains("FinishScope")){

					if(currentThread.getGlobalId() != 0){
						finishNode fin = new finishNode(newObjectID, currentThread);
						fin.setDisplay_name(makeLabel(currentThread,newObjectID));
						graph.addVertex(fin);
						addContinuationEdge(currentNode, fin, graph);
						finishNode end = new finishNode(newObjectID + "-end", currentThread);
						end.setDisplay_name(makeLabel(currentThread,newObjectID+"-end") );
						graph.addVertex(end);

						//create next node for the task
						activityNode nextNode = createNextNode(currentThread);
						graph.addVertex(nextNode);
						addContinuationEdge(fin, nextNode, graph);

						currentNodes.put(currentThread, nextNode);

						//add finish node to the finish stack
						finishBlocks.get(currentThread).push(fin);
					}else{
						Node activity = null;
						Set<Node> nodes = graph.vertexSet();
						for(Node n : nodes){
							if(n.id.startsWith("SuspendableActivity") && n.id.endsWith("-1")){
								activity = n;
							}
						}

						//Master Finish Start
						finishNode fin = new finishNode(newObjectID, currentThread);
						fin.setDisplay_name(makeLabel(currentThread,newObjectID));
						graph.addVertex(fin);
						masterFin = fin;

						//Master Finish end
						finishNode fin_end = new finishNode(newObjectID+"-end", currentThread);
						fin_end.setDisplay_name(makeLabel(currentThread,newObjectID+"-end"));
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
		public void objectReleased(VM vm, ThreadInfo currentThread,
				ElementInfo releasedObject) {

			if (releasedObject.toString().startsWith(runtime)) {

				String objectName = extractObjectName(releasedObject);
				if (objectName.startsWith("SuspendableActivity")) {
					//add edge to master fin end
					Node activity = currentNodes.get(currentThread);
					addContinuationEdge(activity, masterFinEnd, graph);
					System.out.println("Checking Graph " + createGraph(graph, dir, vm));
                    long time = System.currentTimeMillis();
                    System.out.println("GraphSize: " + graph.vertexSet().size());
					if(drd){
						race = analyzeFinishBlock(graph, masterFin.id, on_the_fly);
					}
                    if(zip_drd) {
                        race = ZipperAnalyzer.analyze(graph,orderedIsolatedNodes, numberOfAsyncEdges);
                    }
                    timeAnalyzing += System.currentTimeMillis() - time;
				}
			}
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
				graph.addVertex(isolNode);

				currentNodes.put(currentThread, isolNode);

				addContinuationEdge(currentActivity, isolNode, graph);

				if(previousIsolatedNode != null) {
					addIsolatedEdge(previousIsolatedNode, isolNode, graph);
				}
				previousIsolatedNode = isolNode;
                orderedIsolatedNodes.add(isolNode);

			} else if (methodName.startsWith("stopIsolation")) {

				Node isolatedNode = currentNodes.get(currentThread);
				activityNode nextNode = createNextNode(currentThread);
				graph.addVertex(nextNode);
				addContinuationEdge(isolatedNode, nextNode, graph);

				currentNodes.put(currentThread, nextNode);

			} else if (methodName.startsWith("stopFinish")) {
				String threadID = extractThreadName(currentThread);

				if(finishBlocks.get(currentThread).size() == 1){
					if(!threadID.contains("Suspendable")){
						Node currentNode = currentNodes.get(currentThread);
						Node fin = finishScope.get(threadID);
						String finishJoin = fin.id;
						Node finishJoinNode = searchGraph(finishJoin+"-end", graph);
						addJoinEdge(currentNode, finishJoinNode, graph);
					}
				}else if(finishBlocks.get(currentThread).size() > 1){
					createFinEndNode(currentThread);
				}
			} else if(enteredMethod.getBaseName().equals(futureGet)){

				String futureThreadName = extractCalleeName(currentThread);

				Node oldActNode = currentNodes.get(currentThread);
				Node newActNode = createNextNode(currentThread);
				graph.addVertex(newActNode);
				addContinuationEdge(oldActNode, newActNode, graph);

				currentNodes.put(currentThread, newActNode);

				if(!futureJoinNode.containsKey(futureThreadName)){
					futureJoinNode.put(futureThreadName, newActNode);
				}else{
					addJoinEdge(futureJoinNode.get(futureThreadName), newActNode, graph);
				}

				updateTaskCount();
			}
		}

	@Override
		public void methodExited(VM vm, ThreadInfo currentThread, MethodInfo enteredMethod) {

			if(enteredMethod.getName().contains("stopFinish") && on_the_fly &&
					drd) {
                long time = System.currentTimeMillis();
				race = analyzeFinishBlock(graph,
						finishBlocks.get(currentThread).pop().id, on_the_fly);
                timeAnalyzing += System.currentTimeMillis() - time;
			}
		}

	@Override
		public String getErrorMessage () {
			return "Data Race detected";
		}

	@Override
		public boolean check(Search search, VM vm) {
           // if (race){
           //     System.out.println("Race Detected but ignored for test");
           //     race = false;
           // }
            //System.out.println("Printed a new Graph! " + createGraph(graph,dir,vm));
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

	private void createFinEndNode(ThreadInfo currentThread){

		activityNode currentNode = (activityNode) currentNodes.get(currentThread);

		finishNode start = finishBlocks.get(currentThread).pop();

		finishNode end = (finishNode) searchGraph(start.id+"-end", graph);
		addContinuationEdge(currentNode, end, graph);

		currFinNode.put(currentThread, start);

		//create next node for the task
		activityNode nextNode = createNextNode(currentThread);
		graph.addVertex(nextNode);

		currentNodes.put(currentThread, nextNode);
		addContinuationEdge(end, nextNode, graph);

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
		//	createGraph(graph, dir, search.getVM());
        //    if(zip_drd) {
        //        race = ZipperAnalyzer.analyze(graph, orderedIsolatedNodes, maxNumberOfTasks);
        //    }
		//	if(drd){
		//		race = analyzeFinishBlock(graph, masterFin.id, on_the_fly);
		//	}
		//	check(search, search.getVM());
		//	
            System.out.println("Time Analizing: " + timeAnalyzing);
			System.out.println("Number of Tasks Analysed: " + maxNumberOfTasks);
			System.out.println("Max Number of Active Tasks: " + maxNumberOfActiveTasks);
		}

	
	public static void updateTaskCount() {
		int curNumTasks = (numberOfAsyncEdges + numberOfFutureEdges);
		int curNumActiveTasks = (numberOfAsyncEdges + numberOfFutureEdges - numberOfJoinEdges);
		maxNumberOfTasks = (maxNumberOfTasks > curNumTasks) ? maxNumberOfTasks : curNumTasks;
		maxNumberOfActiveTasks = (maxNumberOfActiveTasks > curNumActiveTasks) ? maxNumberOfActiveTasks : curNumActiveTasks;
	}

	public static void setGraph(DirectedAcyclicGraph<Node, DefaultEdge> graph) {
		CGRaceDetector.graph = graph;
	}

	public static DirectedAcyclicGraph<Node, DefaultEdge> getGraph() {
		return graph;
	}
}
