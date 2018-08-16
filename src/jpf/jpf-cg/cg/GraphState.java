package cg;

import java.util.*;

import gov.nasa.jpf.vm.*;

import org.jgraph.graph.DefaultEdge;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.Graphs;

public class GraphState {
  public isolatedNode previousIsolatedNode = null;

  public Map<ThreadInfo, Node> currentNodes = new HashMap<ThreadInfo, Node>();
  public Map<ThreadInfo, Stack<finishNode>> finishBlocks = new HashMap<ThreadInfo, Stack<finishNode>>();

  public Map<String, Node> finishScope = new HashMap<String, Node>();

  public Map<String, Node> futureJoinNode = new HashMap<String, Node>();

  public DirectedAcyclicGraph<Node, DefaultEdge> graph =
      new DirectedAcyclicGraph<Node, DefaultEdge>(DefaultEdge.class);

  public boolean race = false;
  public finishNode masterFinEnd = null;
  public finishNode masterFin = null;

  public Map<ThreadInfo, finishNode> currFinNode = new HashMap<ThreadInfo, finishNode>();

  public int numberOfAsyncEdges;
  public int numberOfFutureEdges;
  public int numberOfJoinEdges;

  public GraphState(CGRaceDetector rd) {
    this.currentNodes = new HashMap<ThreadInfo, Node>(rd.currentNodes);

    this.finishBlocks = new HashMap<>();
    for (ThreadInfo ti : rd.finishBlocks.keySet()) {
      this.finishBlocks.put(ti,(Stack<finishNode>) rd.finishBlocks.get(ti).clone());
    }


    this.finishScope = new HashMap<String, Node>(rd.finishScope);
    this.futureJoinNode = new HashMap<String, Node>(rd.futureJoinNode);
    this.graph = new DirectedAcyclicGraph<Node, DefaultEdge>(DefaultEdge.class);
    Graphs.addGraph(this.graph, rd.graph);

    this.race = rd.race;
    this.masterFinEnd = rd.masterFinEnd;
    this.masterFin = rd.masterFin;
    this.previousIsolatedNode = rd.previousIsolatedNode;
    this.numberOfAsyncEdges = rd.numberOfAsyncEdges;
    this.numberOfFutureEdges = rd.numberOfFutureEdges;
    this.numberOfJoinEdges = rd.numberOfJoinEdges;
  }

  public void resetGraphState(CGRaceDetector rd) {
    rd.currentNodes = new HashMap<ThreadInfo, Node>(this.currentNodes);

    Map<ThreadInfo, Stack<finishNode>> newBlocks = new HashMap<>();
    for (ThreadInfo ti : this.finishBlocks.keySet()) {
      newBlocks.put(ti,(Stack<finishNode>)this.finishBlocks.get(ti).clone());
    }
    rd.finishBlocks = newBlocks;


    rd.finishScope = new HashMap<String, Node>(this.finishScope);
    rd.futureJoinNode = new HashMap<String, Node>(this.futureJoinNode);
    rd.graph = new DirectedAcyclicGraph<Node, DefaultEdge>(DefaultEdge.class);
    Graphs.addGraph(rd.graph, this.graph);

    rd.race = this.race;
    rd.masterFinEnd = this.masterFinEnd;
    rd.masterFin = this.masterFin;
    rd.previousIsolatedNode = this.previousIsolatedNode;
    rd.numberOfAsyncEdges = this.numberOfAsyncEdges;
    rd.numberOfFutureEdges = this.numberOfFutureEdges;
    rd.numberOfJoinEdges = this.numberOfJoinEdges;
}
}
