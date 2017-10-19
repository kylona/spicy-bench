package cg;

import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgraph.graph.DefaultEdge;
import org.jgrapht.Graphs;
import java.util.*;

public class RaceGraph {
  private static RaceGraph currentGraph = null;
  private static Set<RaceGraph> allGraphs = new HashSet<RaceGraph>(); //stores all graphs created for analisis
  private static int graphCount = 0;
  private int graphNumber;
  public DirectedAcyclicGraph<Node,DefaultEdge> graph;

  public RaceGraph(DirectedAcyclicGraph<Node,DefaultEdge> inputGraph) {
    graphNumber = graphCount;
    graphCount++;
    graph = new DirectedAcyclicGraph(DefaultEdge.class);
    Graphs.addGraph(graph, inputGraph); //copy the given graph into graph
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

}
