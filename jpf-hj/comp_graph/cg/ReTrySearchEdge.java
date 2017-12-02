package cg;


import org.jgraph.graph.DefaultEdge;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.Graphs;

import static cg.Edges.*;
import static cg.Comp_Graph.*;


public class ReTrySearchEdge {

  private final DirectedAcyclicGraph<Node, DefaultEdge> graph;
  private final String finishJoin;
  private final Node currentNode;

  public ReTrySearchEdge(Node currentNode, String finishJoin, DirectedAcyclicGraph<Node, DefaultEdge> graph) {
    this.graph = graph;
    this.finishJoin = finishJoin;
    this.currentNode = currentNode;
  }

  public void retry() {
    Node finishJoinNode = searchGraph(finishJoin+"-end", graph);
    addJoinEdge(currentNode, finishJoinNode, graph);
  };
}
