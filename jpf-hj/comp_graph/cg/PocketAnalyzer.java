package cg;

import java.io.*;
import java.util.*;
import static cg.Edges.*;
import org.jgrapht.traverse.DepthFirstIterator;
import org.jgraph.graph.DefaultEdge;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;

public class PocketAnalyzer {
    private static boolean race = false;
    private static boolean searchFinished = false;
    private static DirectedAcyclicGraph<Node, DefaultEdge> graph;
    private static final boolean DOWN = true;
    private static final boolean UP = false;

    private static class Bag extends HashSet<Pocket> {

    }


    public static class Pocket extends HashSet<Node> {
        private boolean zipped;
        private HashSet<Node> sAfterUp;
        private HashSet<Node> sAfterDown; 
        
        DirectedAcyclicGraph<Node, DefaultEdge> graph;

        public boolean isZipped() {
            return zipped;
        }

        public void setZipped(boolean zipped) {
            this.zipped = zipped;
        }

        public void updateZipDown(Node n) {
            if(!this.zipped && this.sAfterDown.contains(n)) {
                this.zipped = true;
            }
        }
        public void updateZipUp(Node n) {
            if(!this.zipped && this.sAfterUp.contains(n)) {
                this.zipped = true;
            }
        }

        public Pocket(Container<Node> data, Container<Node> sAfterUp, Container<Node> sAfterDown) {
             super(data);
             if (sAfterUp != null) this.sAfterUp = new HashSet(sAfterUp);
             if (sAfterDown != null) this.sAfterDown = new HashSet(sAfterDown);
        }
    }

    public static boolean analyzeGraphForDataRace(DirectedAcyclicGraph<Node, DefaultEdge> graph) {
        PocketAnalyzer.race = false;
        PocketAnalyzer.searchFinished = false;
        PocketAnalyzer.graph = graph;
        Node start = graph.iterator().next();
        Bag pBag = new Bag();
        Bag result = recursiveAnalyze(start, pBag, new ArrayList<Node>());
        return false;
    }

    private static Bag recursiveAnalyze(Node n, Bag pBag, List<Node> seriesNodes) {
        Bag sBag = new Bag();

        if (n.isAsync()) {
            Bag asyncBag = new Bag(pBag);
            for (Node child : n.getChildren()) {
                prepForDownCheck(asyncBag);
                List<Node> childSeries = new ArrayList<Node>();
                Bag seriesResult = recursiveAnalyze(child, asyncBag, childSeries);
                checkForMissingNodes(childSeries, seriesResult);
                checkForPocketIntersect(seriesResult);
                sBag = union(sBag, seriesResult); 
                asnycBag = union(asyncBag, seriesResult);
            }
            n.setReadyForJoin(true);
            Bag joinResult = recursiveAnalyze(getJoin(n), pBag, new ArrayList<Node>());
            sBag = union(sBag, joinResult);
            return sBag;
        }

        if (n.isJoin()) {
            if (getAsync(n).isReadyForJoin()) {
                return recursiveAnalyze(getChild(n));
            }
            else {
                seriesNodes.clear();
                return new sBag();
            }
        }
        seriesNodes.add(n);

        if (n.isIsolation() && getIsolationNodesAfter(n).size() != 0) {
            Set<Nodes> isolationNodesAfter = getIsolationNodesAfter(n);
            if (!isolationNodesAfter.isEmpty()) {
                for (Node i : getIsolationNodesAfter(n)) {
                    Set<Node> sAfterDown = new HashSet();
                    sAfterDown.add(i);
                    sAfterDown.add(getJoin(i));
                }
                newPocket = new Pocket(seriesNodes, null, sAfterDown);
                sBag.add(newPocket);

            }
        }
        
        checkForDataRaceDown(pBag, n);//check all down bags

        Node child = getChild(n);
        Bag resultBag = recursiveAnalyze(child, pBag, seriesNodes);
        
        checkForDataRaceUp(pBag, n);//check all up bags

        if (n.isIsolation() && getIsolationNodesBefore(n).size() != 0) {
            for (Node i : getIsolationNodesBefore(n)) {
                Set<Node> seriesAfter = new HashSet();
                seriesAfter.add(i);
                seriesAfter.add(getAsync(i));
                Pocket newPocket = new Pocket(seriesNodes, UP, seriesAfter);
                sBag.add(newPocket);
            }
        }
        return union(sBag, resultBag);
    }

    private static boolean checkForConflicts(Node first, Node Second) {
        //TODO
        return false;
    }
    private static boolean checkForDataRaceDown(Bag pBag, Node n) {
        boolean dataRace = false;
        for (Pocket pocket : pBag) {
            pocket.updateZipDown(n);
            if (pocket.isZipped()) continue;
            for (Node p : pocket) {
                if (checkForConflicts(n, p)) {
                    dataRace = true;
                }
            }
        }
        return dataRace;
    }
    private static boolean checkForDataRaceUp(Bag pBag, Node n) {
        boolean dataRace = false;
        for (Pocket pocket : pBag) {
            pocket.updateZipUp(n);
            if (pocket.isZipped()) continue;
            for (Node p : pocket) {
                if (checkForConflicts(n, p)) {
                    dataRace = true;
                }
            }
        }
        return dataRace;
    }

    private static Node getAsync(Node n) {
        //TODO
        return n;
    }

    private static Node getJoin(Node n) {
        //TODO
        return n;
    }

    private static Node getChild(Node n) {
        if (getChildren(n).size() == 1) {
            return getChildren(n).iterator().next();
        }
    }

    private static Set<Node> getChildren(Node n) {
        Set<DefaultEdge> outgoing = graph.outgoingEdgesOf(n);
        Set<Node> children = new HashSet<Node>();
        Node edgeSource;
        for(DefaultEdge e : outgoing) {
            Node child = (Node) graph.getEdgeTarget(e);
            children.add(child);
        }
        return children;
    }

    private static void prepForDownCheck(Bag pBag) {
        for (Pocket pocket : pBag) {
            if ((pocket.sAfterDown != null && pocket.sAfterUp != null) ||
                (pocket.sAfterDown == null && pocket.sAfterUp == null) ||
                (pocket.sAfterDown != null && pocket.sAfterUp == null)) {
                    pocket.setZipped(false);
            } else if (pocket.sAfterDown == null && pocket.sAfterUp != null) {
                    pocket.setZipped(true);
            } else {
                throw new RuntimeException();
            }
        }
    }

    private static void prepForUpCheck(Bag pBag) {
        for (Pocket pocket : pBag) {
            if ((pocket.sAfterDown != null && pocket.sAfterUp != null) ||
                (pocket.sAfterDown == null && pocket.sAfterUp == null) ||
                (pocket.sAfterDown == null && pocket.sAfterUp != null)) {
                    pocket.setZipped(false);
            } else if (pocket.sAfterDown != null && pocket.sAfterUp == null) {
                    pocket.setZipped(true);
            } else {
                throw new RuntimeException();
            }
        }
    }

    private static Bag union(Bag... bags) {
        Bag union = new Bag();
        for(Bag b : bags) {
            union.addAll(b);
        }
        return union;
    }
}
