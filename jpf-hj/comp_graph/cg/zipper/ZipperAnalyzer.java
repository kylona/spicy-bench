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
    private static Set<Node> visisted = new HashSet<>();

    private class Pocket extends HashSet<Node> { }

    private static class Zipper<T> extends ArrayList<T> {
        List<Pocket> upPockets;
        List<Pocket> downPockets;
    }

    private static class IsolationZipper<T> extends Zipper<T> {
        int upZip;
        int downZip;
        void checkDown(Node n, SBag sBag, PBag pBag) {
            for (Integer lambdaIdx : pBag.lambdaSet) {
                for (Node c : lambdaZipper.downPockets.get(lambdaIdx)) {
                    sBag.checkSet.add(new Pair<Node,Node>(n,c));
            }
        }
    }

    private class SBag {
        int id;
        List<Integer> isolationNodes;
        Set<Node> seriesSet;
        Set<Pair<Node, Node>> checkSet;

        SBag(int id) {
            this.id = id;
            this.seriesSet = new HashSet<>();
            this.checkSet = new HashSet<>();
        }
    }

    private class PBag {
        Set<Integer> lambdaSet; //reference in lambdaZipper for what to check
        SortedList<Integer> isolationSet; //reference in isolationZipper for what to check

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
                lambdaZipper.upPockets.set(newSBag.id, newSBag.seriesSet);
                maxZip = (maxZip > isolationZipper.upZip) ? maxZip : isolationZipper.upZip;
                minZip = (minZip < isolationZipper.downZip) ? minZip : isolationZipper.downZip;
                asyncBag.add(newId);
            }
            isolationZipper.downZip = minZip;
            n.setReadyForJoin(true);
            Node parentJoin = recursiveAnalyze(join, sBag, pBag);
            isolationZipper.upZip = maxZip;
            visited.add(n);
            return parentJoin;
        }

        else if (n.isJoin()) {
            if (parentsFinished(n)) {
                visited.add(n);
                return recursiveAnalyze(getChild(n),sBag,pBag);
            }
            labdaZipper.downPockets.set(sBag.id, sBag.seriesSet);
            sBag.seriesSet = new HashSet<Node>();
            return n;
        } 

        else {
            sBag.seriesSet.add(n);
            if (n.isIsolated()) {
                isolationZipper.downPockets.set(n.getIndex(), sBag.seriesSet);
                sBag.seriesSet = new HashSet<Node>();
                isolationZipper.downZip = n.getIndex();
                sBag.isolationNodes.add(n.getIndex());

            }

            isolationZipper.checkDown(n, sBag.checkSet, pBag);

            Node join = recursiveAnalyze(getChild(n), sBag, pBag);

            isolationZipper.checkUp(n, sBag.checkSet);

            sBag.seriesSet.add(n);

            if (n.isIsolated()) {
                isolationZipper.upPockets.set(n.getIndex(), sBag.seriesSet);
                sBag.seriesSet = new HashSet<Node>();
                isolationZipper.upZip = n.getIndex();
            }
            return join;
        }

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

    static int counter = 0;

    private static int generateNewId() {
        return counter++;
    }

}
