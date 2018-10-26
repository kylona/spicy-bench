package extensions.zipper;

import extensions.util.StructuredParallelRaceDetector;
import extensions.util.StructuredParallelRaceDetectorTool;
import extensions.compgraph.CompGraphTool;
import extensions.compgraph.CompGraphNode;
import extensions.compgraph.CompGraph;
import gov.nasa.jpf.vm.VM;
import org.jgraph.graph.DefaultEdge;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.Graphs;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class ZipperTool extends CompGraphTool {
  List<CompGraphNode> isolationOrder = new ArrayList<>(); //used for Zipper

  public ZipperTool() {
    super();
  }

  @Override 
  public void resetState(Object state) {
    ZipperToolState toolState = (ZipperToolState)state;
    graph = toolState.graph;
    isolatedNode = toolState.isolatedNode;
    isolationOrder = toolState.isolationOrder;
    currentNodes = toolState.currentNodes;
    tasks = toolState.tasks;
  }

  public Object getImmutableState() {
    return new ZipperToolState(graph, isolatedNode, isolationOrder, currentNodes, tasks);
  }

  @Override
  public void handleAcquire(int tid) {
    CompGraphNode isoNode = CompGraphNode.mkIsolatedNode();
    graph.addVertex(isoNode);
    graph.addContinuationEdge(currentNodes.get(tid), isoNode);
    currentNodes.put(tid, isoNode);
    isolationOrder.add(isoNode);
    if (isolatedNode != null)
      graph.addIsolatedEdge(isolatedNode, isoNode);
    isolatedNode = isoNode;
  }

  public List<CompGraphNode> getIsolationOrder() {
    return isolationOrder;
  }

  class ZipperToolState {
    final CompGraph graph;
    final CompGraphNode isolatedNode;
    final Map<Integer, CompGraphNode> currentNodes;
    final List<CompGraphNode> isolationOrder; //used for zipper 
    final int tasks;
    ZipperToolState(CompGraph graph, CompGraphNode isolatedNode, List<CompGraphNode> isolationOrder, Map<Integer, CompGraphNode> currentNodes, int tasks) {
      this.graph = new CompGraph();
      Graphs.addGraph(this.graph, graph);
      this.isolatedNode = isolatedNode;
      this.isolationOrder = isolationOrder;
      this.currentNodes = new HashMap<>(currentNodes);
      this.tasks = tasks;
    }

    @Override
    public String toString() {
      return "";
    }
  }
}
