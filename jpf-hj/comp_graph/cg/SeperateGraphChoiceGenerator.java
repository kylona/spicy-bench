package cg;
import gov.nasa.jpf.vm.choice.ThreadChoiceFromSet;
import gov.nasa.jpf.vm.*;

import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgraph.graph.DefaultEdge;
import org.jgrapht.Graphs;
import java.util.*;


public class SeperateGraphChoiceGenerator extends ThreadChoiceFromSet {
  private int numberOfChoices;
  private Map<ThreadInfo, DirectedAcyclicGraph<Node, DefaultEdge>> graphMap;
  private Map<ThreadInfo, Integer> debugMap;
  private int currentGraphDebug = -1;

  public SeperateGraphChoiceGenerator(String id, ThreadInfo[] set, boolean isSchedulingPoint, DirectedAcyclicGraph<Node, DefaultEdge> graph) {
    super(id,set,isSchedulingPoint);
    numberOfChoices = this.getTotalNumberOfChoices();
    graphMap = new HashMap<ThreadInfo,  DirectedAcyclicGraph<Node, DefaultEdge>>();
    debugMap = new HashMap<ThreadInfo,  Integer>();

    for (int i = 0; i < numberOfChoices; i++) {
      DirectedAcyclicGraph baby = new DirectedAcyclicGraph(DefaultEdge.class);
      Graphs.addGraph(baby, graph); //copy the given graph into the baby
      graphMap.put(set[i],baby); //assosiate the new copy with a specific choice
      debugMap.put(set[i],i);
    }
  }

  public DirectedAcyclicGraph<Node, DefaultEdge> getGraph(ThreadInfo ti) {
    if (currentGraphDebug != debugMap.get(ti)) {
      currentGraphDebug = debugMap.get(ti);
      System.out.println("Switching to Graph number:" + debugMap.get(ti));
    }

    return graphMap.get(ti);
  }
}
