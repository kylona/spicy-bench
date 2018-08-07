package cg;

import cg.Node;
import org.jgraph.graph.DefaultEdge;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.alg.TarjanLowestCommonAncestor;

import static cg.Edges.*;

import java.util.*;

public class ZipperAnalyzer {

    public static boolean analyze(DirectedAcyclicGraph<Node, DefaultEdge> graph, List<? extends Node> isolationNodes, int numThreads) {
        numThreads = numThreads*20 + 100; //max possible lambda size and buffer don't know why
        ZipperAnalyzer.isolationNodes = (List<Node>) isolationNodes;
        ZipperAnalyzer.graph = graph;
        isolationZipper = new IsolationZipper(isolationNodes.size());
        lambdaZipper = new Zipper(numThreads);
        int newId = generateNewId();
        BranchTrace bt = new BranchTrace(newId);
        Bag sBag = new Bag();
        Bag pBag = new Bag();
        Node n = graph.iterator().next();
        race = false;
        foundBottom = false;
        try {
            recursiveAnalyze(n, bt,sBag, pBag);
        }
        catch (DataRaceException e) {
            System.out.println(e.message);
            return true;
        }
        return false;
    }

    private static DirectedAcyclicGraph<Node, DefaultEdge> graph;
    private static List<Node> isolationNodes;
    private static IsolationZipper<Integer> isolationZipper;
    private static Zipper<Integer> lambdaZipper;
    private static Set<Node> visited = new HashSet<>();
    private static boolean race;
    private static boolean foundBottom = false;
    private static final int NULL = -1;

    private static class Zipper<T> extends ArrayList<T> {
        List<Set<Node>> upPockets;
        List<Set<Node>> downPockets;
        public Zipper(int numThreads) {
            super(numThreads);
            this.upPockets = new ArrayList<Set<Node>>();
            this.downPockets = new ArrayList<Set<Node>>();
            for (int i = 0; i < numThreads; i++) {
                upPockets.add(new HashSet<Node>());
                downPockets.add(new HashSet<Node>());
            }
        }
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < upPockets.size(); i++) {
                sb.append(upPockets.get(i) + "|" + i + "|" + downPockets.get(i) + "\n");
            }
            return sb.toString();
        }
    }

    private static class IsolationZipper<T> extends Zipper<T> {
        int upZip = NULL;
        int downZip = NULL;
        IsolationZipper(int size) {
            super(size);
        }
    }

    private static class BranchTrace {
        int id;
        List<Integer> isolationNodes;
        Set<Node> seriesSet;
        Set<CheckPair> checkSet;

        BranchTrace(int id) {
            this.id = id;
            this.isolationNodes = new ArrayList<Integer>();
            this.seriesSet = new HashSet<>();
            this.checkSet = new HashSet<>();
        }
    }

    private static class Bag extends HashMap<Integer, List<Integer>> {

        public Bag() {
            super();
        }
        public Bag(Bag toCopy) {
            super(toCopy);
        }
    }

    private static class CheckPair {
        public Node first;
        public Node second;
        public int hashCode() {
            int code = first.hashCode() + second.hashCode();
            return code;
        }
        @Override
        public boolean equals(Object pair) {
            if (pair == null) return false;
            if (!(pair instanceof CheckPair)) return false;
            CheckPair p = (CheckPair) pair;
            return first.equals(p.first) && second.equals(p.second) ||
                   first.equals(p.second) && second.equals(p.first);
        }
        public CheckPair(Node first, Node second) {
            this.first = first;
            this.second = second;
        }
        public String toString() {
            return "(" + first.toString() + ", " + second.toString() + ")";
        }
    }

    public static Node recursiveAnalyze(Node n, BranchTrace bt, Bag sBag, Bag pBag) throws DataRaceException {
        if (n.isAsync()) {
            Bag asyncBag = new Bag(pBag);

            int upZipStart = isolationZipper.upZip;
            int downZipStart = isolationZipper.downZip;
            int highZip = upZipStart;
            int lowZip = downZipStart;

            Node join = null;
            n.setReadyForJoin(false);
            for (Node child : getChildren(n)) {
                int newId = generateNewId();
                BranchTrace newBranchTrace = new BranchTrace(newId);
                isolationZipper.upZip = upZipStart;//reset to same as start
                isolationZipper.downZip = downZipStart;
                join = recursiveAnalyze(child, newBranchTrace, sBag, asyncBag);
                lambdaZipper.upPockets.set(newBranchTrace.id, newBranchTrace.seriesSet);
                highZip = (highZip < isolationZipper.upZip) ? highZip : isolationZipper.upZip;
                lowZip = (lowZip > isolationZipper.downZip) ? lowZip : isolationZipper.downZip;
                sBag.put(newId, newBranchTrace.isolationNodes);
                asyncBag.putAll(sBag);
            }
            isolationZipper.downZip = lowZip;
            n.setReadyForJoin(true);
            Node parentJoin = recursiveAnalyze(join, bt, sBag, pBag);
            isolationZipper.upZip = highZip;
            visited.add(n);
            return parentJoin;
        }

        else if (n.isJoin()) {
            if (getAsync(n).isReadyForJoin()) {
                Node child = getChild(n);
                visited.add(n);
                if (child != null) return recursiveAnalyze(child,bt,sBag,pBag);
                else return n;
            }
            lambdaZipper.downPockets.set(bt.id, bt.seriesSet);
            bt.seriesSet = new HashSet<Node>();
            return n;
        } 

        else {
            bt.seriesSet.add(n);
            if (n.isIsolated() && n.hasOutgoingIsolationEdge()) {
                isolationZipper.downPockets.set(n.getIndex(), bt.seriesSet);
                bt.seriesSet = new HashSet<Node>();
                isolationZipper.downZip = n.getIndex();
                bt.isolationNodes.add(n.getIndex());
            }

            checkDown(n, bt, pBag);

            Node child = getChild(n);
            Node join = null;
            if (child != null) {
                join = recursiveAnalyze(getChild(n), bt, sBag, pBag);
            }

            checkUp(n, bt, pBag);

            bt.seriesSet.add(n);

            if (n.isIsolated() && n.hasIncomingIsolationEdge()) {
                isolationZipper.upPockets.set(n.getIndex(), bt.seriesSet);
                bt.seriesSet = new HashSet<Node>();
                isolationZipper.upZip = n.getIndex();
            }
            visited.add(n);
            return join;
        }

    }

    private static Map<Node,Node> asyncMap = new HashMap<>();
    private static TarjanLowestCommonAncestor<Node,DefaultEdge> tjCalc = null;
    private static Node getAsync(Node n) {
        if (asyncMap.containsKey(n)) return asyncMap.get(n);
        if (tjCalc == null) tjCalc = new TarjanLowestCommonAncestor<>(graph);
        Set<DefaultEdge> incoming = graph.incomingEdgesOf(n);
        Node async = n;
        for (DefaultEdge e : incoming) {
            Node parent = (Node) graph.getEdgeSource(e);
            async = tjCalc.calculate(graph.iterator().next(), async, parent);
        }
        asyncMap.put(n,async);
        return async;
    }

    private static boolean checkRace(Node a, Node b) throws DataRaceException {
        System.out.println("Considering " + a + " and " + b);
        if (!(a instanceof activityNode) || !(b instanceof activityNode))
            return false;
        activityNode first = (activityNode) a;
        activityNode second = (activityNode) b;
        return checkForDataAccessConflicts(first.var_write, second.var_write) ||
            checkForDataAccessConflicts(first.var_write, second.var_read) ||
            checkForDataAccessConflicts(first.var_read, second.var_write) ||
            checkForDataAccessConflicts(first.isolated_write, second.var_write) ||
            checkForDataAccessConflicts(first.var_write, second.isolated_write) ||
            checkForDataAccessConflicts(first.isolated_write, second.var_read) ||
            checkForDataAccessConflicts(first.var_read, second.isolated_write) ||
            checkForDataAccessConflicts(first.array_write, second.array_write) ||
            checkForDataAccessConflicts(first.array_write, second.array_read) ||
            checkForDataAccessConflicts(first.array_read, second.array_write) ||
            checkForDataAccessConflicts(first.array_write_isolated, second.array_write) ||
            checkForDataAccessConflicts(first.array_write, second.array_write_isolated) ||
            checkForDataAccessConflicts(first.array_write_isolated, second.array_read) ||
            checkForDataAccessConflicts(first.array_read, second.array_write_isolated);
    }

    private static String message;
    private static boolean checkForDataAccessConflicts(List<? extends DataAccess> first, List<? extends DataAccess> second) throws DataRaceException {
        if (first == null || second == null)
            return false;
        for (DataAccess dataAccess1 : first) {
            for (DataAccess dataAccess2 : second) {
                if (dataAccess1.conflictsWith(dataAccess2)) {
                    message = "Non-deterministic access between\n\t" +
                    dataAccess1.toString() + "\n\t" + dataAccess2.toString();
                    reportRace(message);
                    return true;
                }
            }
        }
        return false;
    }
    private static void checkDown(Node n, BranchTrace bt, Bag pBag) throws DataRaceException {
        for (int branchId : pBag.keySet()) {
            for (Node c : lambdaZipper.downPockets.get(branchId)) {
                bt.checkSet.add(new CheckPair(n,c));
            }
            for (int isolationIdx : pBag.get(branchId)) {
                //TODO: traverse the list greatest to least
                if (isolationZipper.downZip != NULL && isolationIdx <= isolationZipper.downZip) continue;
                for (Node c : isolationZipper.downPockets.get(isolationIdx)) {
                    CheckPair newPair = new CheckPair(n,c);
                    bt.checkSet.add(newPair);
                }
            }
        }
    }

    private static void checkUp(Node n, BranchTrace bt, Bag pBag) throws DataRaceException {
        for (int branchId : pBag.keySet()) {
            for (Node c : lambdaZipper.upPockets.get(branchId)) {
                CheckPair newPair = new CheckPair(n,c);
                if (bt.checkSet.contains(newPair)) {
                    checkRace(n,c);
                }
            }
            for (int isolationIdx : pBag.get(branchId)) {
                //traverse the list least to greatest
                if (isolationIdx >= isolationZipper.upZip) break;
                for (Node c : isolationZipper.upPockets.get(isolationIdx)) {
                    if (bt.checkSet.contains(new CheckPair(n,c))) {
                        checkRace(n,c);
                    }
                }

            }
            
        }
    }
    private static Node getChild(Node n) {
        if (getChildren(n).size() == 1) {
            return getChildren(n).iterator().next();
        } else {
            if (foundBottom) {
                throw new RuntimeException("getChild called on node with more than one child");
            }
            else {
                foundBottom = true;
                return null;
            }
        }
    }

    private static Set<Node> getChildren(Node n) {
        Set<DefaultEdge> outgoing = graph.outgoingEdgesOf(n);
        Set<Node> children = new HashSet<Node>();
        Node edgeSource;
        for (DefaultEdge e : outgoing) {
            if (e.getAttributes().equals(IsolatedEdgeAttributes())) {
                continue;
            }
            Node child = (Node) graph.getEdgeTarget(e);
            children.add(child);
        }
        return children;
    }

    static int counter = 0;

    private static int generateNewId() {
        return counter++;
    }

    private static void reportRace(String message) throws DataRaceException {
        race = true;
        throw new DataRaceException(message);
    }

    private static class DataRaceException extends Exception {
        
        String message;
        public DataRaceException(String message) {
            this.message = message;
        }
    }

}
