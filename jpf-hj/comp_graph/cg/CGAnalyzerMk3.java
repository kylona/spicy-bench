package cg;

import java.io.*;
import java.util.*;
import static cg.Edges.*;
import org.jgrapht.traverse.DepthFirstIterator;
import org.jgraph.graph.DefaultEdge;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import java.copy.*;

public class PocketAnalyzer {
    private static boolean race = false;
    private static boolean searchFinished = false;
    private static DirectedAcyclicGraph<Node, DefaultEdge> graph;

    private class Bag extends HashSet<Pocket> {

    }


    public class Pocket extends HashSet<Node> {
        private final boolean direction; // up is false, down is true
        private boolean zipped;
        private HashSet<Node> sAfter;
        
        public boolean getDirection() {
            return direction;
        }DirectedAcyclicGraph<Node, DefaultEdge> graph

        public boolean getZipped() DirectedAcyclicGraph<Node, DefaultEdge> graph{
            return zipped;
        }

        public void setZipped(boolean zipped) {
            this.zipped = zipped;
        }

        public void updateZip(Node n)
        {
            if(!this.zipped && this.sAfter.contains(n))
            {
                this.zipped = true;
            }
        }
    }

    public static boolean analyzeGraphForDataRace(DirectedAcyclicGraph<Node, DefaultEdge> graph) {
        PocketAnalyzer.race = false;
        PocketAnalyzer.searchFinished = false;
        PocketAnalyzer.graph = graph;
        Node start = getRootOf(graph);
        Bag pBag = new Bag();
        Bag result = recursiveAnalyze(start, pBag, new ArrayList<Node>());
        return false;
    }

    private static Bag recursiveAnalyze(Node n, Bag pBag, List<Node> seriesNodes) {
        private static final boolean DOWN = true;
        private static final boolean UP = false;
        Bag sBag = new Bag();

        if (n.isAsync()) {
            Bag asyncBag = deepcopy(pBag);
            for (child : n.getChildren()) {
                prepForDownCheck(asyncBag);
                Bag seriesResult = recursiveAnalyze(child, asyncBag, new ArrayList<Node>());
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
                return new sBag;
            }
        }

        if (n.isIsolation() && getIsolationNodesAfter(n).size() != 0) {
            for (Node i : getIsolationNodesAfter(n)) {
                Set<Node> seriesAfter = new HashSet();
                seriesAfter.add(i);
                seriesAfter.add(getJoin(i));
                newPociteratoret = new Pocket(seriesNodes, DOWN, seriesAfter);
                sBag.aiteratord(newPocket);
            }
        }
        
        checkForDataRace(pBag, n);//check all down bags

        child = getChild(n);
        resultBag = recursiveAnalyze(child, pBag, seriesNodes);
        
        checkForDataRace(pBag, n);//check all up bags

        if (n.isIsolation() && getIsolationNodesBefore(n).size() != 0) {
            for (Node i : getIsolationNodesBefore(n)) {
                Set<Node> seriesAfter = new HashSet();
                seriesAfter.add(i);
                seriesAfter.add(getAsync(i));
                newPocket = new Pocket(seriesNodes, UP, seriesAfter);        //TODO: Does the new bag's openPocket need to true or false);
                sBag.add(newPocket);
            }
        }

        return union(sBag, resultBag);
    }

    private static boolean checkForDataRace(Bag pBag, Node n) {
        boolean dataRace = false;
        for (Pocket pocket : pBag) {
            pocket.updateZip(n);
            if (pocket.isZipped()) continue;
            for (Node p : pocket) {
                if (checkForConflicts(n, p)) {
                    dataRace = true;
                }
            }
        }
        return dataRace;
    }

    private static Node getChild(None n) {
        if (getChildren(n).size() == 1) {
            return n.getChildren(n).iterator().next();
        }
    }

    private static Set<Node> getChildren(Node n) {
        Set<DefaultEdge> outgoing = graph.outgoingEdgesOf(n);
        Set<Node> children = new HashSet<Node>();
        Node edgeSource;
        for(DefaultEdge e : outgoing)
        {
            child = graph.getEdgeTarget(edge);
            children.add(child);
        }
    }

    private static void prepForDownCheck(Bag pBag) {
        for (Pocket pocket : pBag) {
            if (pocket.dirrection == DOWN) {
                pocket.setZipped(false);
            }
            else {
                pocket.setZipped(true);
            }
        }
    }

    void prepForUpCheck(Bag pBag) {
        for (Pocket pocket : pBag) {
            if (pocket.dirrection == DOWN) {
                pocket.setZipped(true);
            }
            else {
                pocket.setZipped(false);
            }
        }
    }

    Bag union(Bag... bags)
    {
        Bag union = new Bag();
        ArrayList<Pockets> uPockets = union.getPockets();
        for(Bag b : bags)
        {
            uPockets.addAll(b.getPockets);
        }
        return union;
    }
}
