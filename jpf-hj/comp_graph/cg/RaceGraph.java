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
	private Stack<finishNode> finishBlocks = null;

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

    this.finishBlocks = new Stack<finishNode>();
    this.finishBlocks.addAll(inputGraph.finishBlocks);

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

   public Stack<finishNode> getFinishBlocks() {
     return finishBlocks;
   }

   public void setFinishBlocks(Stack<finishNode> finishBlocks) {
     this.finishBlocks = finishBlocks;
   }

}
