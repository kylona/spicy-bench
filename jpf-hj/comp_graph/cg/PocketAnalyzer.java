package cg;

import java.util.*;

import org.jgraph.graph.DefaultEdge;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;

public class PocketAnalyzer {
    private static boolean race = false;
    private static boolean searchFinished = false;
    private static DirectedAcyclicGraph<Node, DefaultEdge> graph;
    private static final boolean DOWN = true;
    private static final boolean UP = false;

    private static class Bag extends HashSet<Pocket> {

        public Bag(Bag toCopy) {
            super(toCopy);
        }

        public Bag() {
            super();
        }
    }


    public static class Pocket extends HashSet<Node> {
        private boolean zipped;
        HashSet<Node> sAfterUp;
        HashSet<Node> sAfterDown;

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

        public Pocket(Collection<Node> data, Collection<Node> sAfterUp, Collection<Node> sAfterDown) {
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

        if (n.isAsync()) {
            Bag asyncBag = new Bag(pBag);
            Bag sBag = new Bag();
            for (Node child : getChildren(n)) {
                prepForDownCheck(asyncBag);
                List<Node> childSeries = new ArrayList<Node>();
                Bag seriesResult = recursiveAnalyze(child, asyncBag, childSeries);
                checkForPocketIntersect(seriesResult);
                checkForMissingNodes(childSeries, seriesResult);
                sBag = union(sBag, seriesResult);
                asyncBag = union(asyncBag, seriesResult);
            }
            n.setReadyForJoin(true);
            Bag joinResult = recursiveAnalyze(getJoin(n), pBag, new ArrayList<Node>());
            sBag = union(sBag, joinResult);
            for (Pocket pocket : sBag) {
                pocket.updateZipUp(n);
            }
            return sBag;
        }

        if (n.isJoin()) {
            if (getAsync(n).isReadyForJoin()) {
                for (Pocket pocket : pBag) {
                    pocket.updateZipDown(n);
                }
                return recursiveAnalyze(getChild(n), pBag, seriesNodes);
            }
            else {
                seriesNodes.clear();
                return new Bag();
            }
        }

        seriesNodes.add(n);
        Bag sBag = new Bag();

        if (n.isIsolated() && getIsolationNodesAfter(n).size() != 0) {
            Set<Node> isolationNodesAfter = getIsolationNodesAfter(n);
            if (!isolationNodesAfter.isEmpty()) {
                Set<Node> sAfterDown = new HashSet();
                for (Node i : isolationNodesAfter) {
                    sAfterDown.add(i);
                    sAfterDown.add(getJoin(i));
                }
                Pocket newPocket = new Pocket(seriesNodes, null, sAfterDown);
                sBag.add(newPocket);
            }
        }

        checkForDataRaceDown(pBag, n);//check all down bags

        Node child = getChild(n);
        Bag resultBag = recursiveAnalyze(child, pBag, seriesNodes);

        checkForDataRaceUp(pBag, n);//check all up bags

        if (n.isIsolated() && getIsolationNodesBefore(n).size() != 0) {
            Set<Node> isolationNodesBefore = getIsolationNodesBefore(n);
            if (!isolationNodesBefore.isEmpty()) {
                Set<Node> sAfterUp = new HashSet();
                for (Node i : isolationNodesBefore) {
                    sAfterUp.add(i);
                    sAfterUp.add(getJoin(i));
                }
                Pocket newPocket = new Pocket(seriesNodes, sAfterUp, null);
                sBag.add(newPocket);
            }
        }
        return union(sBag, resultBag);
    }

    private static boolean checkForConflicts(Node a, Node b) {
        if (!(a instanceof activityNode) || !(b instanceof activityNode))
            return false;
        activityNode first = (activityNode) a;
        activityNode second = (activityNode) b;
        return checkForDataAccessConflicts(first.var_write, second.var_write) ||
            checkForDataAccessConflicts(first.var_write, second.var_read) ||
            checkForDataAccessConflicts(first.var_read, second.var_write) ||
            checkForDataAccessConflicts(first.isolated_write, second.isolated_write) ||
            checkForDataAccessConflicts(first.isolated_write, second.isolated_read) ||
            checkForDataAccessConflicts(first.isolated_read, second.isolated_write) ||
            checkForDataAccessConflicts(first.isolated_write, second.var_write) ||
            checkForDataAccessConflicts(first.var_write, second.isolated_write) ||
            checkForDataAccessConflicts(first.isolated_write, second.var_read) ||
            checkForDataAccessConflicts(first.var_read, second.isolated_write) ||
            checkForDataAccessConflicts(first.array_write, second.array_write) ||
            checkForDataAccessConflicts(first.array_write, second.array_read) ||
            checkForDataAccessConflicts(first.array_read, second.array_write) ||
            checkForDataAccessConflicts(first.array_write_isolated, second.array_write_isolated) ||
            checkForDataAccessConflicts(first.array_write_isolated, second.array_read_isolated) ||
            checkForDataAccessConflicts(first.array_read_isolated, second.array_write_isolated) ||
            checkForDataAccessConflicts(first.array_write_isolated, second.array_write) ||
            checkForDataAccessConflicts(first.array_write, second.array_write_isolated) ||
            checkForDataAccessConflicts(first.array_write_isolated, second.array_read) ||
            checkForDataAccessConflicts(first.array_read, second.array_write_isolated);
    }

    private static boolean checkForDataAccessConflicts(List<? extends DataAccess> first, List<? extends DataAccess> second) {
        if (first == null || second == null)
            return false;
        for (DataAccess dataAccess1 : first)
            for (DataAccess dataAccess2 : second)
                if (dataAccess1.conflictsWith(dataAccess2))
                    return true;
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

    private static void checkForMissingNodes(Collection<Node> childSeries, Bag seriesResult) {
        for (Pocket pocket : seriesResult) {
            for (Node n : pocket) {
                childSeries.remove(n);
            }
        }
        if (childSeries.isEmpty()) return;
        Pocket newPocket = new Pocket(childSeries, null, null);
        seriesResult.add(newPocket);
    }

    private static void checkForPocketIntersect(Bag seriesResult) {
        for (Pocket first : seriesResult) {
            for (Pocket second : seriesResult) {
                //boolean firstIsUp = first.sAfterUp!=null && first.sAfterDown==null;
                boolean firstIsDown = first.sAfterUp==null && first.sAfterDown!=null;
                boolean secondIsUp = second.sAfterUp!=null && second.sAfterDown==null;
                //boolean secondIsDown = second.sAfterUp==null && second.sAfterDown!=null;
                if (firstIsDown && secondIsUp) {
                    Set<Node> intersect = new HashSet<Node>(first);
                    intersect.retainAll(second);
                    first.removeAll(intersect);
                    Pocket newPocket = new Pocket(intersect,first.sAfterDown,second.sAfterUp);
                    seriesResult.add(newPocket);
                    if (first.isEmpty()) {
                        seriesResult.remove(first);
                    } else {
                        System.out.println("Aparently this does happen");
                    }
                }
            }
        }
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
        } else throw new RuntimeException("getChild called on node with more than one child");
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

    private static Set<Node> getIsolationNodesBefore(Node n) {
        if (!n.isIsolated()) throw new RuntimeException();

        Set<Node> result = null;
        for (DefaultEdge e : graph.incomingEdgesOf(n)) {
            if (e.getAttributes().equals(Edges.IsolatedEdgeAttributes())) {
                if (result != null) throw new RuntimeException("Isolation Node has more than one outgoing edge");
                Node i = (Node) graph.getEdgeSource(e);
                result = getIsolationNodesBefore(i);
                result.add(n);
            }
        }
        return result;
    }

    private static Set<Node> getIsolationNodesAfter(Node n) {
        if (!n.isIsolated()) throw new RuntimeException();

        Set<Node> result = null;
        for (DefaultEdge e : graph.outgoingEdgesOf(n)) {
            if (e.getAttributes().equals(Edges.IsolatedEdgeAttributes())) {
                if (result != null) throw new RuntimeException("Isolation Node has more than one outgoing edge");
                Node i = (Node) graph.getEdgeTarget(e);
                result = getIsolationNodesAfter(i);
                result.add(n);
            }
        }
        return result;
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
