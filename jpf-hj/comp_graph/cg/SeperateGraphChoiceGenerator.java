package cg;
import gov.nasa.jpf.vm.choice.ThreadChoiceFromSet;
import gov.nasa.jpf.vm.*;

import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgraph.graph.DefaultEdge;
import org.jgrapht.Graphs;
import java.util.*;


public class SeperateGraphChoiceGenerator extends ThreadChoiceFromSet {
  private int numberOfChoices;
  private int currentGraphDebug = -1; //stores number of current graph for reporting when graphs are switched
  private RaceGraph resetGraph;
  private Map<ThreadInfo, RaceGraph> graphMap; //maps choices to graphs




  public SeperateGraphChoiceGenerator(String id, ThreadInfo[] set, boolean isSchedulingPoint, RaceGraph graph) {
    super(id,set,isSchedulingPoint);
    numberOfChoices = this.getTotalNumberOfChoices();
    graphMap = new HashMap<ThreadInfo, RaceGraph>();
    resetGraph = new RaceGraph(graph);
    for (int i = 0; i < numberOfChoices; i++) {
      graphMap.put(set[i],new RaceGraph(graph)); //assosiate the new copy with a specific choice
    }
  }

  public RaceGraph getGraph(ThreadInfo ti) {
    if (currentGraphDebug != graphMap.get(ti).getGraphNumber()) {
      currentGraphDebug = graphMap.get(ti).getGraphNumber();
      System.out.println("Switching to Graph number:" + graphMap.get(ti).getGraphNumber());
    }
    return graphMap.get(ti);
  }

  @Override
  public void advance() {
    super.advance();
    if (!hasMoreChoices()) { //if we have finished our job
      RaceGraph.setCurrentGraph(resetGraph);
    }
  }

}
