package cg;

import java.io.*;
import java.util.*;
import static cg.Edges.*;
import org.jgrapht.traverse.DepthFirstIterator;
import org.jgraph.graph.DefaultEdge;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;

public class Bag
{
    private ArrayList<Pocket> pockets;
    private Pocket openPocket;
    
    public ArrayList<Pocket> getPockets()
    {
        return pockets;
    }

    public void addPocket(Pocket p)
    {
        pockets.add(p);
    }

    public Pocket getOpenPocket()
    {
        return openPocket;
    }

    public void setOpenPocket(Pocket openPocket)
    {
        this.openPocket = openPocket;
    }
}

public class Pocket
{
    private boolean direction; // up is false, down is true
    private boolean zipped;
    private HashSet<Nodes> sAfter;
    private HashSet<Nodes> inBag; 
    
    public boolean getDirection()
    {
        return direction;
    }

    public void setDirection(boolean direction)
    {
        this.direction = direction;
    }

    public boolean getZipped()
    {
        return zipped;
    }

    public void setZipped(boolean zipped)
    {
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


public class CGAnalyzer {
    private static boolean race = false;
    private static Stack<Node> AsyncStack = new Stack<Node>();
    private static Map<Node, Node> asyncToJoin = new HashMap<Node, Node>();
    private static boolean searchFinished = false;

    public static boolean analyzeGraphForDataRace(DirectedAcyclicGraph<Node, DefaultEdge> graph) {
        CGAnalyzer.race = false;
        CGAnalyzer.searchFinished = false;
        Node start = getRootOf(graph);
        Set<ValidatedSet> parallelAccesses = new HashSet<ValidatedSet>();
        Set<ValidatedSet> seriesAccesses = new HashSet<ValidatedSet>();
        ValidatedSet activeSet = new ValidatedSet();
        seriesAccesses.add(activeSet);
        Set<ValidatedSet> result = checkForDataRace(graph,
                                                    start,
                                                    activeSet,
                                                    parallelAccesses,
                                                    seriesAccesses);
        return false;

    }

    Bag unionBag(Bag... bags)
    {
        //TODO: Does the new bag's openPocket need to true or false
        Bag union = new Bag();
        ArrayList<Pockets> uPockets = union.getPockets();
        for(Bag b : bags)
        {
            uPockets.addAll(b.getPockets);
        }
        return union;
    }

    Bag recursiveAnalyze(Node n, Bag pBag)
    {
        if(n.isAsync())
        {
            //shifting down
            for(Pocket p : pBag.getPockets())
            {
                if(p.getDirection())
                {
                    p.setZipped(false);
                }
                else
                {
                    p.setZipped(true);
                }
            }

            Bag seriesLeft = recursiveAnalyze(leftChild, pBag);
            Bag asyncBag = unionBag(pBag, seriesLeft);

            for(Pocket p : asyncBag.getPockets)
            {
                if(p.getDirection())
                {
                    p.setZipped(false);
                }
                else
                {
                    p.setZipped(true);
                }
            }

            Bag seriesRight = recursiveAnalyze(rightChild, asyncBag);
            n.setReadyForJoin(true);

            for(Pocket p : pBag)
            {
                if(p.getDirection())
                {
                    p.setZipped(false);
                }
                else
                {
                    p.setZipped(true);
                }
            }

            //note that this is the original pBag without all the extra series stuff
            Bag seriesJoin = recursiveAnalyze(getJoin(n), pBag); 
            return unionBag(seriesRight, seriesLeft, seriesJoin);
        }

        if(n.isJoin())
        {
            // Need to not include asyncs from nested finish-blocks 
            
            //WILL NEED TO TEST THIS
            //TODO: update to Tarjan with jgrapht
            boolean readyForJoin = true;
            Set<DefaultEdge> incoming = graph.incomingEdgesOf(n);
            Node edgeSource;
            for(DefaultEdge e : incoming)
            {
                edgeSource = graph.getEdgeSource(edge);
                if(!edgeSource.getReadyForJoin())
                {
                    readyForJoin = false;
                    break;
                }
            }
            Set<DefaultEdge> outgoing = graph.outgoingEdgesOf(n);
            if(readyForJoin && (outgoing.size() != 0))
            {
                //TODO: Find a better way to accomplish this, There should be only one child
                Iterator<DefaultEdge> it = outgoing.iterator();
                Node child = it.next().getEdgeTarget();
                return recursiveAnalyze(child, pBag);
            }
            else
            {
                for(Pocket p : pBag)
                {
                    if(p.getDirection())
                    {
                        p.setZipped(true);
                    }
                    else
                    {
                        p.setZipped(false);
                    }
                }

                Bag sBag = new Bag(); 
                emptyPocket = new Pocket();
                sBag.add(emptyPocket);
                sBag.setOpenPocket(emptyPocket);
                return sBag;    
            }
        }

        //Not a join or async node;

        //Check for data races on way down
        for(Pocket p : pBag)
        {
            p.updateZip(n); //if unzipped && n in S-after: zip
            //TODO check with Kyle for this line
            if(!pocket.getZipped())
            {
                
            }
        }

        Bag sBag = recursiveAnalyze(child, pBag);
        Pocket openPocket = sBag.getOpenPocket();

        for(Pocket p : pBag)
        {
            p.updateZip(n);
            //TODO check with Kyle for this line
            if(!pocket.getZipped())
            {

            }
        }

        //continue building sBag
        if(n.isIsolated())
        {
            if(false) //TODO fix to match pseudocode
            {
                openPocket.setDirection(false); //up is false, down is true
                //TODO Check with kyle on 
            }
        }

    }

}