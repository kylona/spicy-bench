package cg;

public class ZipperAnalyzer {

    private Zipper<Node> isolationZipper = new Zipper
    private class Pocket extends HashSet<Node> {

    }

    private class isolationZipper {
        int upZip;
        int downZip;
        ArrayList<Pocket> upPockets;
        ArrayList<Pocket> downPockets;
        ArrayList<Node> isolationNodes;
    }

    private class lambdaZipper {
        ArrayList<Pocket> upPockets;
        ArrayList<Pocket> downPockets;
    }

    private class CheckPair {
        Node first;
        Node second;

        public CheckPair(Node first, Node second) {
            this.first = first;
            this.second = second;
        }
    }


    private class SBag {
        int id;
        Set<Node> seriesSet;
        Set<CheckPair> checkSet;

        public sBag(int id) {
            this.id = id;
            this.seriesSet = new HashSet<Node>();
            this.checkSet = new HashSet<CheckPair>();
        }
    }
    
    private class PBag extends TreeSet<int> {

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

            n.isReadyForJoin = false;

            Node join = null;
            for (Node child : getChldren(n)) {
                newId = generateNewId();   
                newSBag = new SBag(newId);
                isloationZipper.upZip = upZipStart;//reset to same as start
                isolationZipper.downZip = downZipStart;
                join = recursiveAnalyze(child, newSBag, asyncBag);
                maxZip = (maxZip > isolationZipper.upZip) ? maxZip : isolationZipper.upZip;
                minZip = (minZip < isolationZipper.downZip) ? minZip : isolationZipper.downZip;
                asyncBag.add(newId);
            }
            isolationZipper.downZip = minZip;
            n.isReadyForJoin = true;
            Node parentJoin = recursiveAnalyze(join,sBag, pBag);
            isolationZipper.upZip = maxZip;

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

    static int counter = 0;

    private static int generateNewId() {
        return counter++;
    }

}
