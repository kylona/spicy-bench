package cg;

import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgraph.graph.DefaultEdge;
import org.jgrapht.Graphs;
import java.util.*;

import gov.nasa.jpf.vm.choice.ThreadChoiceFromSet;
import gov.nasa.jpf.vm.*;

public class RaceGraph {

  private static RaceGraph currentGraph = null;//new RaceGraph("blah", new DirectedAcyclicGraph<Node,DefaultEdge>(DefaultEdge.class)); //start with a blank graph
  private static Set<RaceGraph> allGraphs = new HashSet<RaceGraph>(); //stores all graphs created for analisis
  private static int graphCount = 0;
  private int graphNumber;
  public DirectedAcyclicGraph<Node,DefaultEdge> graph;

  public isolatedNode previousIsolatedNode = null;

	public Map<ThreadInfo, Node> currentNodes = new HashMap<ThreadInfo, Node>();
	private Map<ThreadInfo, Stack<finishNode>> finishBlocks = new HashMap<ThreadInfo, Stack<finishNode>>();

	public Map<String, Node> finishScope = new HashMap<String, Node>();

	public Map<String, Node> futureJoinNode = new HashMap<String, Node>();

	public finishNode masterFinEnd = null;
	public finishNode masterFin = null;

	public Map<ThreadInfo, finishNode> currFinNode = new HashMap<ThreadInfo, finishNode>();

  public static Node timeout = new activityNode("TIMEOUT");

  public RaceGraph(DirectedAcyclicGraph<Node,DefaultEdge> inputGraph) {
    graphNumber = graphCount;
    graphCount++;
    graph = new DirectedAcyclicGraph(DefaultEdge.class);
    Graphs.addGraph(graph, inputGraph); //copy the given graph into graph
    graph.addVertex(timeout);
    allGraphs.add(this);
  }

  public RaceGraph(RaceGraph inputGraph) {
    graphNumber = graphCount;
    graphCount++;
    graph = new DirectedAcyclicGraph(DefaultEdge.class);
    Graphs.addGraph(graph, inputGraph.graph); //copy the given graph into graph
    this.currentNodes = new HashMap<ThreadInfo, Node>(inputGraph.currentNodes);

    HashMap<ThreadInfo, Stack<finishNode>> newBlocks = new HashMap<>();
    HashMap<ThreadInfo, Stack<finishNode>> blocksCopy = new HashMap<>();
    for (Map.Entry<ThreadInfo, Stack<finishNode>> entry : inputGraph.finishBlocks.entrySet()) {
      newBlocks.put(entry.getKey(), new Stack<finishNode>());
      blocksCopy.put(entry.getKey(), new Stack<finishNode>());
      Stack<finishNode> stackToCopy = entry.getValue();
      List<finishNode> stackCopy = new LinkedList<>();
      while (!stackToCopy.isEmpty()) {
        finishNode elem = stackToCopy.pop();
        stackCopy.add(0, elem);
      }
      for (finishNode node : stackCopy) {
        newBlocks.get(entry.getKey()).push(node);
        blocksCopy.get(entry.getKey()).push(node);
      }
    }
    this.finishBlocks = newBlocks;
    inputGraph.finishBlocks = blocksCopy;

    this.finishScope = new HashMap<String, Node>(inputGraph.finishScope);
    this.futureJoinNode = new HashMap<String, Node>(inputGraph.futureJoinNode);
    this.masterFinEnd = inputGraph.masterFinEnd;
    this.masterFin = inputGraph.masterFin;
    this.currFinNode = new HashMap<ThreadInfo, finishNode>(inputGraph.currFinNode);
    allGraphs.add(this);
  }

  public int getGraphNumber() {
    return graphNumber;
  }


  public static RaceGraph getCurrentGraph() {
    if (currentGraph == null) {
      currentGraph = new RaceGraph(new DirectedAcyclicGraph<Node,DefaultEdge>(DefaultEdge.class));
    }
    return currentGraph;
  }

  public static void setCurrentGraph(RaceGraph cg) {
    currentGraph = cg;
  }

  public static Set<RaceGraph> getGraphSet() {
    return allGraphs;
  }

  public static void printAllGraphs() {
    for (RaceGraph rg : allGraphs) {
      System.out.println("\n\nGraph Number " + rg.graphNumber + ":\n");
      Comp_Graph.printGraph(rg.graph);
    }
  }

   public Map<ThreadInfo, Stack<finishNode>> getFinishBlocks() {
     
     return finishBlocks;
   }

}
