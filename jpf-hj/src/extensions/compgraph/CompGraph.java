package extensions.compgraph;

import gov.nasa.jpf.vm.VM;
import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.DefaultEdge;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.ext.ComponentAttributeProvider;
import org.jgrapht.ext.DOTExporter;
import org.jgrapht.ext.IntegerNameProvider;
import org.jgraph.graph.GraphConstants;

import java.awt.Color;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CompGraph extends DirectedAcyclicGraph<CompGraphNode, DefaultEdge> {
  public static final long serialVersionUID = -1;

  CompGraphNode first = null;
  public CompGraph() {
    super(DefaultEdge.class);
  }

  public boolean happensBefore(CompGraphNode n1, CompGraphNode n2) {
    if (n1 == n2)
      return true;
    for (DefaultEdge edge : outgoingEdgesOf(n1)) {
      CompGraphNode child = getEdgeTarget(edge);
      if (happensBefore(child, n2))
        return true;
    }
    return false;
  }

  public boolean isIsolatedEdge(DefaultEdge e) {
    return e.getAttributes().equals(IsolatedEdgeAttributes());
  }

  public static <V, E> AttributeMap SpawnEdgeAttributes() {
    AttributeMap map = new AttributeMap();
    GraphConstants.setLineColor(map, Color.GREEN);
    GraphConstants.setLineStyle(map, 2);
    return map;
  }

  public static <V, E> AttributeMap JoinEdgeAttributes() {
    AttributeMap map = new AttributeMap();
    GraphConstants.setLineColor(map, Color.RED);
    return map;
  }

  public static <V, E> AttributeMap IsolatedEdgeAttributes() {
    AttributeMap map = new AttributeMap();
    GraphConstants.setLineColor(map, Color.PINK);
    return map;
  }

  public static <V, E> AttributeMap ContinuationEdgeAttributes() {
    AttributeMap map = new AttributeMap();
    GraphConstants.setLineColor(map, Color.BLACK);
    return map;
  }

  public void addSpawnEdge(CompGraphNode n1, CompGraphNode n2) {
    DefaultEdge e1 = null;
    e1 = addEdge(n1, n2);
    e1.setAttributes(SpawnEdgeAttributes());
  }

  public void addJoinEdge(CompGraphNode n1, CompGraphNode n2) {
    DefaultEdge e1 = null;
    e1 = addEdge(n1, n2);
    e1.setAttributes(JoinEdgeAttributes());
  }

  public void addContinuationEdge(CompGraphNode n1, CompGraphNode n2) {
    DefaultEdge e1 = null;
    e1 = addEdge(n1, n2);
    e1.setAttributes(ContinuationEdgeAttributes());
  }

  public void addIsolatedEdge(CompGraphNode n1, CompGraphNode n2) {
    DefaultEdge e1 = null;
    e1 = addEdge(n1, n2);
    e1.setAttributes(IsolatedEdgeAttributes());
    n1.setHasOutgoingIsolationEdge(true);
    n2.setHasIncomingIsolationEdge(true);
  }

  public void writeGraph(String fname) {
    IntegerNameProvider<CompGraphNode> p1 = new IntegerNameProvider<CompGraphNode>();
    ComponentAttributeProvider<CompGraphNode> p2 = new ComponentAttributeProvider<CompGraphNode>() {
      @Override
      public Map<String, String> getComponentAttributes(CompGraphNode arg0) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("label", String.valueOf(arg0.getIndex()));
        return map;
      }
    };

    ComponentAttributeProvider<DefaultEdge> p3 = new ComponentAttributeProvider<DefaultEdge>() {
      @Override
      public Map<String, String> getComponentAttributes(DefaultEdge arg0) {
        Map<String, String> map = new HashMap<String, String>();
        if (arg0.getAttributes().equals(SpawnEdgeAttributes())) {
            map.put("color", "green");
        } else if (arg0.getAttributes().equals(JoinEdgeAttributes())) {
            map.put("color", "red");
        } else if (arg0.getAttributes().equals(IsolatedEdgeAttributes())) {
            map.put("color", "pink");
        } else if (arg0.getAttributes().equals(ContinuationEdgeAttributes())) {
            map.put("color", "black");
        } else {
            map.put("color", "black");
        }
        return map;
      }
    };

    DOTExporter<CompGraphNode, DefaultEdge> exporter = new DOTExporter<CompGraphNode, DefaultEdge>(p1, null, null, p2, p3);
    try {
        exporter.export(new FileWriter(fname), this);
    } catch (IOException e) {
        e.printStackTrace();
    }
  }
}

