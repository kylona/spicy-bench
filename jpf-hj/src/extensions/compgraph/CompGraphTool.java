package extensions.compgraph;

import extensions.util.StructuredParallelRaceDetector;
import extensions.util.StructuredParallelRaceDetectorTool;
import gov.nasa.jpf.vm.VM;
import org.jgraph.graph.DefaultEdge;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.Graphs;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

public class CompGraphTool implements StructuredParallelRaceDetectorTool {
  protected CompGraph graph = new CompGraph();
  protected Map<Integer, CompGraphNode> currentNodes = new HashMap<>();
  protected CompGraphNode isolatedNode = null;
  protected int tasks = 0;

  public CompGraphTool() {
    CompGraphNode root = CompGraphNode.mkActivityNode();
    graph.addVertex(root);
    currentNodes.put(0, root);
  }

  public void resetState(Object state) {
    CompGraphToolState toolState = (CompGraphToolState)state;
    graph = toolState.graph;
    isolatedNode = toolState.isolatedNode;
    currentNodes = toolState.currentNodes;
    tasks = toolState.tasks;
  }

  public Object getImmutableState() {
    return new CompGraphToolState(graph, isolatedNode, currentNodes, tasks);
  }

  //void ensureActivityNode(int tid) {
  //  if (currentNodes.get(tid).isAsync()) {
  //    CompGraphNode continueNode = CompGraphNode.mkActivityNode();
  //    graph.addVertex(continueNode);
  //    graph.addContinuationEdge(currentNodes.get(tid), continueNode);
  //    currentNodes.put(tid, continueNode);
  //  }
  //}

  public void handleRead(int tid, String uniqueLabel) {
    currentNodes.get(tid).addAccess(uniqueLabel, false);
  }

  public void handleWrite(int tid, String uniqueLabel) {
    currentNodes.get(tid).addAccess(uniqueLabel, true);
  }

  public void handleAcquire(int tid) {
    CompGraphNode isoNode = CompGraphNode.mkIsolatedNode();
    graph.addVertex(isoNode);
    graph.addContinuationEdge(currentNodes.get(tid), isoNode);
    currentNodes.put(tid, isoNode);
    if (isolatedNode != null)
      graph.addIsolatedEdge(isolatedNode, isoNode);
    isolatedNode = isoNode;
  }

  public void handleRelease(int tid) {
    CompGraphNode continueNode = CompGraphNode.mkActivityNode();
    graph.addVertex(continueNode);
    graph.addContinuationEdge(currentNodes.get(tid), continueNode);
    currentNodes.put(tid, continueNode);
  }

  public void handleFork(int parent, int child) {
    CompGraphNode forkNode = CompGraphNode.mkForkNode();
    CompGraphNode continueNode = CompGraphNode.mkActivityNode();
    CompGraphNode childNode = CompGraphNode.mkActivityNode();
    graph.addVertex(forkNode);
    graph.addVertex(continueNode);
    graph.addVertex(childNode);
    graph.addContinuationEdge(currentNodes.get(parent), forkNode);
    graph.addSpawnEdge(forkNode, childNode);
    graph.addContinuationEdge(forkNode, continueNode);
    currentNodes.put(parent, continueNode);
    currentNodes.put(child, childNode);
    tasks++;
  }

  public void handleJoin(int parent, int child) {
    if (currentNodes.get(parent).isJoin()) {
      graph.addJoinEdge(currentNodes.get(child), currentNodes.get(parent));
      return;
    }
    CompGraphNode joinNode = CompGraphNode.mkJoinNode();
    graph.addVertex(joinNode);
    graph.addJoinEdge(currentNodes.get(parent), joinNode);
    graph.addJoinEdge(currentNodes.get(child), joinNode);
    currentNodes.put(parent, joinNode);
  }

  public void writeGraph(String fname) {
    graph.writeGraph(fname);
  }

  public CompGraph getGraph() {
    return graph;
  }

  public int getTaskCount() {
    return tasks;
  }

  public boolean race() {
    return false;
  }

  public String error() {
    return "";
  }

  class CompGraphToolState {
    final CompGraph graph;
    final CompGraphNode isolatedNode;
    final Map<Integer, CompGraphNode> currentNodes;
    final int tasks;
    CompGraphToolState(CompGraph graph, CompGraphNode isolatedNode, Map<Integer, CompGraphNode> currentNodes, int tasks) {
      this.graph = new CompGraph();
      Graphs.addGraph(this.graph, graph);
      this.isolatedNode = isolatedNode;
      this.currentNodes = new HashMap<>(currentNodes);
      this.tasks = tasks;
    }

    @Override
    public String toString() {
      return "";
    }
  }
}
