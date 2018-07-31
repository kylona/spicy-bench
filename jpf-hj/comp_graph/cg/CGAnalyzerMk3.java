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

    private class Bag extends HashSet<Pocket> {

    }


    public class Pocket extends HashSet<Node> {
        private final boolean direction; // up is false, down is true
        private boolean zipped;
        private HashSet<Nodes> sAfter;
        
        public boolean getDirection() {
            return direction;
        }

        public boolean getZipped() {
            return zipped;
        }

        public void setZipped(boolean zipped) {
            this.zipped = zipped;
        }
    }

    Bag recursiveAnalyze(Node n, Bag pBag, List<Node> seriesNodes) {
        private static final boolean DOWN = true;
        private static final boolean UP = false;
        
        Bag sBag = new Bag();

        if (n.isAsync()) {
            Bag asyncBag = deepcopy(pBag);
            for (child : n.getChildren()) {
                prepForDownCheck(asyncBag);
                Bag seriesResult = recursiveAnylize(child, asyncBag, new ArrayList<Node>());
                sBag = union(sBag, seriesResult); 
                asnycBag = union(asyncBag, seriesResult);
            }
            n.setReadyForJoin(true);
            Bag joinResult = recursiveAnylize(getJoin(n), pBag, new ArrayList<Node>());
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
                newPocket = new Pocket(seriesNodes, DOWN, seriesAfter);
                sBag.add(newPocket);
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
                newPocket = new Pocket(seriesNodes, UP, seriesAfter);
                sBag.add(newPocket);
            }
        }

        return union(sBag, resultBag);
    }

    boolean checkForDataRace(Bag pBag, Node n) {
        boolean dataRace = false;
        for (Pocket pocket : pBag) {
            updateZip(pocket);
            if (pocket.isZipped()) continue;
            for (Node p : pocket) {
                if (checkForConflicts(n, p)) {
                    dataRace = true;
                }
            }
        }
        return dataRace;
    }

    Node getChild(None n) {
        if (getChildren(n).size() == 1) {
            return n.getChildren(n).iterator().next();
        }
    }

    Set<Node> getChildren(Node n) {

    }

    void prepForDownCheck(Bag pBag) {
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

    Bag union(Bag ... bags) {
        Bag result = new Bag();
        for (Bag bag : bags) {
            for (Pocket pocket : bag) {
                result.add(pocket);
            }
        }
        return result;
    }

    void updateZip(Pocket pocket, Node n) {
        if (pocket.seriesAfter.contains(n)) {
            pocket.zip();
        }
    }

}
