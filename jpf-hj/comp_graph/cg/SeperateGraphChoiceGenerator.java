package cg;
import gov.nasa.jpf.vm.choice.ThreadChoiceFromSet;
import gov.nasa.jpf.vm.*;

import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgraph.graph.DefaultEdge;
import org.jgrapht.Graphs;
import java.util.*;

import static cg.Edges.*;
import static cg.Comp_Graph.*;


public class SeperateGraphChoiceGenerator extends BooleanChoiceGenerator {
  private DirectedAcyclicGraph<Node, DefaultEdge> graph;
  private Node inodeFirst;
  private Node inodeSecond;

  public SeperateGraphChoiceGenerator(String id, DirectedAcyclicGraph<Node, DefaultEdge> graph, Node inodeFirst, Node inodeSecond) {
    super(id);
    this.graph = graph;
    this.inodeFirst = inodeFirst;
    this.inodeSecond = inodeSecond;
    CGRaceDetector.dumpGraph("Start Choice");
  }


  @Override
  public void advance() {



    if (graph.getEdge(inodeFirst,inodeSecond) == null
     && graph.getEdge(inodeSecond,inodeFirst) == null) {
      System.out.println("Trying a before b");
      addIsolatedEdge(inodeFirst, inodeSecond,graph);
      CGRaceDetector.dumpGraph("A->B");
      super.advance();
      return;
    }

    //toggle a to b or b to a
    if (graph.getEdge(inodeFirst,inodeSecond) != null
     && graph.getEdge(inodeSecond,inodeFirst) == null) {
       graph.removeEdge(graph.getEdge(inodeFirst,inodeSecond));
       System.out.println("Trying b before a");
       addIsolatedEdge(inodeSecond, inodeFirst,graph);
       CGRaceDetector.dumpGraph("B->A");
       super.advance();

       return;
    }

    if (graph.getEdge(inodeFirst,inodeSecond) == null
     && graph.getEdge(inodeSecond,inodeFirst) != null) {
       graph.removeEdge(graph.getEdge(inodeSecond,inodeFirst));
       System.out.println("Back to a before b");
       addIsolatedEdge(inodeFirst, inodeSecond,graph);
       CGRaceDetector.dumpGraph("A<<<<B");
       super.advance();

       return;
    }

  }


}
