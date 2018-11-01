package extensions.zipper;
import extensions.compgraph.CompGraphNode;
import extensions.compgraph.CompGraph;

import org.jgraph.graph.DefaultEdge;
import org.jgrapht.alg.TarjanLowestCommonAncestor;

import java.util.*;

public class ZipperCheck {

    private static CompGraph graph;
    private static List<CompGraphNode> isolationNodes;
	private static IsolationZipper<Integer> isolationZipper;
    private static Zipper<Integer> lambdaZipper;
    private static boolean race = false;
    private static boolean foundBottom = false;
    private static final int NULL = -1;

    public boolean check(CompGraph graph, List<CompGraphNode> isolationOrder) {
        ZipperCheck.isolationNodes = isolationOrder;
        ZipperCheck.graph = graph;
        isolationZipper = new IsolationZipper();
        lambdaZipper = new Zipper();
        int newId = generateNewId();
        BranchTrace bt = new BranchTrace(newId);
        Bag sBag = new Bag();
        Bag pBag = new Bag();
        CompGraphNode n = (CompGraphNode) graph.iterator().next();
        race = false;
        foundBottom = false;
        try {
            recursiveAnalyze(n, bt,sBag, pBag);
        }
        catch (DataRaceException e) {
            System.out.println("Yeah that was an exception");
            System.out.println(e.message);
            return true;
        }
        return race;
    }

    private static class Zipper<T> extends ArrayList<T> {
        List<Set<CompGraphNode>> upPockets = new ArrayList<Set<CompGraphNode>>();
        List<Set<CompGraphNode>> downPockets = new ArrayList<Set<CompGraphNode>>();
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < upPockets.size(); i++) {
                sb.append(upPockets.get(i) + "|" + i + "|" + downPockets.get(i) + "\n");
            }
            return sb.toString();
        }
        public Set<CompGraphNode> getUpPocket(int i) {
          int size = upPockets.size();
          if (i >= size) {
            for (int j = 0; j < i - size + 1; j++) {
              upPockets.add(new HashSet<CompGraphNode>());
            }
          }
          return upPockets.get(i);
        }
        public Set<CompGraphNode> getDownPocket(int i) {
          int size = downPockets.size();
          if (i >= size) {
            for (int j = 0; j < i - size + 1; j++) {
              downPockets.add(new HashSet<CompGraphNode>());
            }
          }
          return downPockets.get(i);
        }
        public Set<CompGraphNode> setUpPocket(int i, Set<CompGraphNode> n) {
          int size = upPockets.size();
          if (i >= size) {
            for (int j = 0; j < i - size + 1; j++) {
              upPockets.add(new HashSet<CompGraphNode>());
            }
          }
          return upPockets.set(i, n);
        }
        public Set<CompGraphNode> setDownPocket(int i, Set<CompGraphNode> n) {
          int size = downPockets.size();
          if (i >= size) {
            for (int j = 0; j < i - size + 1; j++) {
              downPockets.add(new HashSet<CompGraphNode>());
            }
          }
          return downPockets.set(i, n);
        }
    }

    private static class IsolationZipper<T> extends Zipper<T> {
        int upZip = NULL;
        int downZip = NULL;
    }

    private static class BranchTrace {
        int id;
        List<Integer> isolationNodes;
        Set<CompGraphNode> seriesSet;
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
        public CompGraphNode first;
        public CompGraphNode second;
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
        public CheckPair(CompGraphNode first, CompGraphNode second) {
            this.first = first;
            this.second = second;
        }
        public String toString() {
            return "(" + first.toString() + ", " + second.toString() + ")";
        }
    }

    public static CompGraphNode recursiveAnalyze(CompGraphNode n, BranchTrace bt, Bag sBag, Bag pBag) throws DataRaceException {
        if (n.isAsync()) {
            Bag asyncBag = new Bag(pBag);

            int upZipStart = isolationZipper.upZip;
            int downZipStart = isolationZipper.downZip;
            int highZip = upZipStart;
            int lowZip = downZipStart;

            CompGraphNode join = null;
            n.setReadyForJoin(false);
            for (CompGraphNode child : getChildren(n)) {
                int newId = generateNewId();
                BranchTrace newBranchTrace = new BranchTrace(newId);
                isolationZipper.upZip = upZipStart;//reset to same as start
                isolationZipper.downZip = downZipStart;
                join = recursiveAnalyze(child, newBranchTrace, sBag, asyncBag);
                lambdaZipper.setUpPocket(newBranchTrace.id, newBranchTrace.seriesSet);
                highZip = (highZip < isolationZipper.upZip) ? highZip : isolationZipper.upZip;
                lowZip = (lowZip > isolationZipper.downZip) ? lowZip : isolationZipper.downZip;
                sBag.put(newId, newBranchTrace.isolationNodes);
                asyncBag.putAll(sBag);
            }
            isolationZipper.downZip = lowZip;
            n.setReadyForJoin(true);
            CompGraphNode parentJoin = recursiveAnalyze(join, bt, sBag, pBag);
            isolationZipper.upZip = highZip;
            return parentJoin;
        }

        else if (n.isJoin()) {
            if (getAsync(n).isReadyForJoin()) {
                CompGraphNode child = getChild(n);
                if (child != null) return recursiveAnalyze(child,bt,sBag,pBag);
                else return n;
            }
            lambdaZipper.setDownPocket(bt.id, bt.seriesSet);
            bt.seriesSet = new HashSet<CompGraphNode>();
            return n;
        } 

        else {
            bt.seriesSet.add(n);
            if (n.isIsolated() && n.hasOutgoingIsolationEdge()) {
                isolationZipper.setDownPocket(n.getIndex(), bt.seriesSet);
                bt.seriesSet = new HashSet<CompGraphNode>();
                isolationZipper.downZip = n.getIndex();
                bt.isolationNodes.add(n.getIndex());
            }

            checkDown(n, bt, pBag);

            CompGraphNode child = getChild(n);
            CompGraphNode join = null;
            if (child != null) {
                join = recursiveAnalyze(getChild(n), bt, sBag, pBag);
            }

            checkUp(n, bt, pBag);

            bt.seriesSet.add(n);

            if (n.isIsolated() && n.hasIncomingIsolationEdge()) {
                isolationZipper.setUpPocket(n.getIndex(), bt.seriesSet);
                bt.seriesSet = new HashSet<CompGraphNode>();
                isolationZipper.upZip = n.getIndex();
            }
            return join;
        }

    }

    private static Map<CompGraphNode,CompGraphNode> asyncMap = new HashMap<>();
    private static TarjanLowestCommonAncestor<CompGraphNode,DefaultEdge> tjCalc = null;
    private static CompGraphNode getAsync(CompGraphNode n) {
        if (asyncMap.containsKey(n)) return asyncMap.get(n);
        if (tjCalc == null) tjCalc = new TarjanLowestCommonAncestor<>(graph);
        Set<DefaultEdge> incoming = graph.incomingEdgesOf(n);
        CompGraphNode async = n;
        for (DefaultEdge e : incoming) {
            CompGraphNode parent = (CompGraphNode) graph.getEdgeSource(e);
            async = tjCalc.calculate(graph.iterator().next(), async, parent);
        }
        asyncMap.put(n,async);
        return async;
    }

    private static boolean checkRace(CompGraphNode a, CompGraphNode b) throws DataRaceException {
       return race = race || (a.isReadWrite() && b.isReadWrite() && a.intersection(b).size() > 0);
    }

    private static void checkDown(CompGraphNode n, BranchTrace bt, Bag pBag) throws DataRaceException {
        for (int branchId : pBag.keySet()) {
            for (CompGraphNode c : lambdaZipper.getDownPocket(branchId)) {
                bt.checkSet.add(new CheckPair(n,c));
            }
            for(int i = pBag.get(branchId).size() - 1; i >= 0; i--) {
                int isolationIdx = pBag.get(branchId).get(i);
                if (isolationZipper.downZip != NULL && isolationIdx <= isolationZipper.downZip) break;
                for (CompGraphNode c : isolationZipper.getDownPocket(isolationIdx)) {
                    CheckPair newPair = new CheckPair(n,c);
                    bt.checkSet.add(newPair);
                }
            }
        }
    }

    private static void checkUp(CompGraphNode n, BranchTrace bt, Bag pBag) throws DataRaceException {
        for (int branchId : pBag.keySet()) {
            for (CompGraphNode c : lambdaZipper.getUpPocket(branchId)) {
                CheckPair newPair = new CheckPair(n,c);
                if (bt.checkSet.contains(newPair)) {
                    checkRace(n,c);
                }
            }
            for (int isolationIdx : pBag.get(branchId)) {
                //traverse the list least to greatest
                if (isolationIdx >= isolationZipper.upZip) break;
                for (CompGraphNode c : isolationZipper.getUpPocket(isolationIdx)) {
                    if (bt.checkSet.contains(new CheckPair(n,c))) {
                        checkRace(n,c);
                    }
                }

            }

        }
    }

    private static CompGraphNode getChild(CompGraphNode n) {
        if (getChildren(n).size() == 1) {
            return getChildren(n).iterator().next();
        } else {
            if (foundBottom) {
                throw new RuntimeException("Found two bottoms. Second at " + n.getIndex());
            }
            else {
                System.out.println("Bottom at " + n.getIndex());
                foundBottom = true;
                return null;
            }
        }
    }

    private static Set<CompGraphNode> getChildren(CompGraphNode n) {
        Set<DefaultEdge> outgoing = graph.outgoingEdgesOf(n);
        Set<CompGraphNode> children = new HashSet<CompGraphNode>();
        CompGraphNode edgeSource;
        for (DefaultEdge e : outgoing) {
            if (graph.isIsolatedEdge(e)) {
                continue;
            }
            CompGraphNode child = (CompGraphNode) graph.getEdgeTarget(e);
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
        System.out.println(message);
        if (false) throw new DataRaceException(message);
    }

    private static class DataRaceException extends Exception {

        String message;
        public DataRaceException(String message) {
            this.message = message;
        }
    }

}
