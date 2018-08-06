package cg.zipper;

import array.Pair;
import cg.Node;
import org.jgraph.graph.DefaultEdge;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;

import java.util.*;

public class ZipperAnalyzer {

  public static void analyze(DirectedAcyclicGraph<Node, DefaultEdge> graph, List<Node> isolationNodes) {
    ZipperAnalyzer.isolationNodes = isolationNodes;
    ZipperAnalyzer.graph = graph;
  }

  private static DirectedAcyclicGraph<Node, DefaultEdge> graph;
  private static List<Node> isolationNodes;
  private static IsolationZipper<Integer> isolationZipper = new IsolationZipper<>();
  private static Zipper<Integer> lambdaZipper = new Zipper<>();

  private class Pocket extends HashSet<Node> { }

  private static class Zipper<T> extends ArrayList<T> {
    List<Pocket> upPockets;
    List<Pocket> downPockets;

    public Pocket getUpPocket(int i) {
      return upPockets.get(i);
    }

    public Pocket getDownPocket(int i) {
      return downPockets.get(i);
    }
  }

  private static class IsolationZipper<T> extends Zipper<T> {
    int upZip;
    int downZip;
  }

  private class SBag {
    int id;
    Set<Node> seriesSet;
    Set<Pair<Node, Node>> checkSet;

    SBag(int id) {
      this.id = id;
      this.seriesSet = new HashSet<>();
      this.checkSet = new HashSet<>();
    }
  }

  private class PBag extends TreeSet<Integer> {

    public PBag(PBag toCopy) {
      super(toCopy);
    }
  }

  public Node recursiveAnalyze(Node n, SBag sBag, PBag pBag) {
    if (n.isAsync()) {
      PBag asyncBag = new PBag(pBag);

      int upZipStart = isolationZipper.upZip;
      int downZipStart = isolationZipper.downZip;
      int maxZip = upZipStart;
      int minZip = downZipStart;

      n.setReadyForJoin(false);

      Node join = null;
      for (Node child : getChldren(n)) {
        int newId = generateNewId();
        SBag newSBag = new SBag(newId);
        isloationZipper.upZip = upZipStart;//reset to same as start
        isolationZipper.downZip = downZipStart;
        join = recursiveAnalyze(child, newSBag, asyncBag);
        maxZip = (maxZip > isolationZipper.upZip) ? maxZip : isolationZipper.upZip;
        minZip = (minZip < isolationZipper.downZip) ? minZip : isolationZipper.downZip;
        asyncBag.add(newId);
      }
      isolationZipper.downZip = minZip;
      n.setReadyForJoin(true);
      Node parentJoin = recursiveAnalyze(join, sBag, pBag);
      isolationZipper.upZip = maxZip;
      return parentJoin;
    }

    if (n.isJoin()) {
       if ( 
    }
    return null;
  }

  private static Node getChild(Node n) {
    if (getChildren(n).size() == 1) {
      return getChildren(n).iterator().next();
    } else {
      throw new RuntimeException("getChild called on node with more than one child");
    }
  }

  private static Set<Node> getChildren(Node n) {
    Set<DefaultEdge> outgoing = graph.outgoingEdgesOf(n);
    Set<Node> children = new HashSet<Node>();
    Node edgeSource;
    for (DefaultEdge e : outgoing) {
      Node child = (Node) graph.getEdgeTarget(e);
      children.add(child);
    }
    return children;
  }
  
  private static getAsync(Node n) {
    
  }

  static int counter = 0;

  private static int generateNewId() {
    return counter++;
  }

}
