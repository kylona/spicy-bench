package cg;

import java.awt.Color;

import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.DefaultEdge;
import org.jgraph.graph.GraphConstants;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph.CycleFoundException;

public class Edges {

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

    public static <V, E> AttributeMap FutureEdgeAttributes() {
        AttributeMap map = new AttributeMap();
        GraphConstants.setLineColor(map, Color.BLUE);
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

    public static <V, E> AttributeMap SignalEdgeAttributes() {
        AttributeMap map = new AttributeMap();
        GraphConstants.setLineColor(map, Color.CYAN);
        return map;
    }

    public static <V, E> AttributeMap WaitEdgeAttributes() {
        AttributeMap map = new AttributeMap();
        GraphConstants.setLineColor(map, Color.pink);
        return map;
    }

    public static <V, E> AttributeMap PhaseEdgeAttributes() {
        AttributeMap map = new AttributeMap();
        GraphConstants.setLineColor(map, Color.cyan);
        return map;
    }

    public static void addSpawnEdge(Node FromNode, Node ToNode, DirectedAcyclicGraph<Node, DefaultEdge> graph) {
        DefaultEdge e1 = null;
        try {
            e1 = graph.addDagEdge(FromNode, ToNode);
        } catch (CycleFoundException e) {
            e.printStackTrace();
        }
        e1.setAttributes(SpawnEdgeAttributes());
	CGRaceDetector.numberOfAsyncEdges++;
    FromNode.setAsync(true);
    }

    public static void addJoinEdge(Node FromNode, Node ToNode, DirectedAcyclicGraph<Node, DefaultEdge> graph) {
        DefaultEdge e1 = null;
        try {
            e1 = graph.addDagEdge(FromNode, ToNode);
        } catch (CycleFoundException e) {
            e.printStackTrace();
        }
        e1.setAttributes(JoinEdgeAttributes());
	CGRaceDetector.numberOfJoinEdges++;
    ToNode.setJoin(true);
    }

    public static void addContinuationEdge(Node FromNode, Node ToNode, DirectedAcyclicGraph<Node, DefaultEdge> graph) {
        DefaultEdge e1 = null;
        try {
            e1 = graph.addDagEdge(FromNode, ToNode);
        } catch (CycleFoundException e) {
            e.printStackTrace();
        }
        if(e1!=null)
        	e1.setAttributes(ContinuationEdgeAttributes());
    }

    public static void addFutureEdge(Node FromNode, Node ToNode, DirectedAcyclicGraph<Node, DefaultEdge> graph) {
        DefaultEdge e1 = null;
        try {
            e1 = graph.addDagEdge(FromNode, ToNode);
        } catch (CycleFoundException e) {
            e.printStackTrace();
        }
        e1.setAttributes(FutureEdgeAttributes());
	CGRaceDetector.numberOfFutureEdges++;
    }

    public static void addIsolatedEdge(Node FromNode, Node ToNode, DirectedAcyclicGraph<Node, DefaultEdge> graph) {
        DefaultEdge e1 = null;
        try {
            e1 = graph.addDagEdge(FromNode, ToNode);
        } catch (CycleFoundException e) {
            e.printStackTrace();
        }
        e1.setAttributes(IsolatedEdgeAttributes());
        FromNode.setIsolated(true);
        FromNode.setOutgoingIsolationEdge(true);
        ToNode.setIsolated(true);
        ToNode.setIncomingIsolationEdge(true);
        if (FromNode.getIndex() == -1) { 
            FromNode.setIndex(0);
        }
        ToNode.setIndex(FromNode.getIndex() + 1);
    }

    public static void addSignalEdge(Node FromNode, Node ToNode, DirectedAcyclicGraph<Node, DefaultEdge> graph) {
        DefaultEdge e1 = null;
        try {
            e1 = graph.addDagEdge(FromNode, ToNode);
        } catch (CycleFoundException e) {
            e.printStackTrace();
        }
        e1.setAttributes(SignalEdgeAttributes());
    }

    public static void addWaitEdge(Node FromNode, Node ToNode, DirectedAcyclicGraph<Node, DefaultEdge> graph) {
        DefaultEdge e1 = null;
        try {
            e1 = graph.addDagEdge(FromNode, ToNode);
        } catch (CycleFoundException e) {
            e.printStackTrace();
        }
        e1.setAttributes(WaitEdgeAttributes());
    }

    public static void addPhaseEdge(Node FromNode, Node ToNode, DirectedAcyclicGraph<Node, DefaultEdge> graph) {
        DefaultEdge e1 = null;
        try {
            e1 = graph.addDagEdge(FromNode, ToNode);
        } catch (CycleFoundException e) {
            e.printStackTrace();
        }
        e1.setAttributes(PhaseEdgeAttributes());
    }
}
