package cg;
import gov.nasa.jpf.vm.choice.ThreadChoiceFromSet;
import gov.nasa.jpf.vm.*;

import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgraph.graph.DefaultEdge;
import org.jgrapht.Graphs;
import java.util.*;

import static cg.Edges.*;
import static cg.Comp_Graph.*;

public class IsolateChoiceGenerator extends ThreadChoiceFromSet {
  private Node previousIsolatedNode = null;
  private DefaultEdge addedEdge = null;
  private Stack<DefaultEdge> edges = new Stack<DefaultEdge>();
  private Stack<Node> vertexes = new Stack<Node>();;
  DirectedAcyclicGraph<Node,DefaultEdge> graph;
  private Map<ThreadInfo, Node> currentNodes;
  private static int idCount;
  private int id;


  public IsolateChoiceGenerator(String id, ThreadInfo[] set, boolean isSchedulingPoint, Map<ThreadInfo, Node> currentNodes, DirectedAcyclicGraph<Node,DefaultEdge> graph) {
    super(id,set,isSchedulingPoint);
    this.currentNodes = currentNodes;
    this.graph = graph;
    this.id = ++idCount;
  }

  @Override
  public void advance() {
    super.advance();

    ThreadInfo currentThread = super.getNextChoice();

    IsolateChoiceGenerator[] previousIsolateChoices = getAllOfType(IsolateChoiceGenerator.class);

    for (IsolateChoiceGenerator choice : previousIsolateChoices) {
      if (choice != this) {
        if (choice.getNextChoice() == currentThread) {
          System.out.println(currentThread);
          if (hasMoreChoices()) {
            advance();//skip this one;
          }
          return;
        }
      }
    }

    activityNode currentActivity = (activityNode) currentNodes.get(currentThread);
    String [] s = currentActivity.id.split("-");
    int next_num = Integer.parseInt(s[1]) + 1;

    isolatedNode isolNode = new isolatedNode("Isolated_" + s[0] + "-" + next_num, currentThread);
    isolNode.setDisplay_name(CGRaceDetector.makeLabel(currentThread,"Isolated@" + next_num));
    graph.addVertex(isolNode);

    currentNodes.put(currentThread, isolNode);

    addContinuationEdge(currentActivity, isolNode, graph);


    if(previousIsolatedNode != null){
      if (addedEdge == null) {
        addedEdge = addIsolatedEdge(previousIsolatedNode, isolNode, graph);
      }
      else {
        graph.removeEdge(addedEdge);
        addedEdge = addIsolatedEdge(previousIsolatedNode, isolNode, graph);
      }
    }
    previousIsolatedNode = isolNode;






  }

}
