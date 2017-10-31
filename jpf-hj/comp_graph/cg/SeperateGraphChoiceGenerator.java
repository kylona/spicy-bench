package cg;
import gov.nasa.jpf.vm.choice.ThreadChoiceFromSet;
import gov.nasa.jpf.vm.*;

import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgraph.graph.DefaultEdge;
import org.jgrapht.Graphs;
import java.util.*;

import static cg.Edges.*;
import static cg.Comp_Graph.*;


public class SeperateGraphChoiceGenerator extends ThreadChoiceFromSet {
  private int numberOfChoices;
  private DirectedAcyclicGraph graph;
  private Node inodeFirst;
  private Node inodeSecond;

  public SeperateGraphChoiceGenerator(String id, ThreadInfo[] set, boolean isSchedulingPoint,DirectedAcyclicGraph<Node, DefaultEdge> graph, Node inodeFirst, Node inodeSecond) {
    super(id,set,isSchedulingPoint);
    System.out.println("created SeperateGraphChoiceGenerator");
    numberOfChoices = this.getTotalNumberOfChoices();
    System.out.println(numberOfChoices);
    this.graph = graph;
    addIsolatedEdge(inodeFirst, inodeSecond,graph);
  }

  @Override
  public void advance() {
    System.out.println("Count:" +count);
    graph.removeEdge(graph.getEdge(inodeFirst,inodeSecond));
    addIsolatedEdge(inodeSecond, inodeFirst,graph);
    super.advance();
  }
}
