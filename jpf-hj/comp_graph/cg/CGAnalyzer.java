package cg;

import java.io.*;
import java.util.*;
import static cg.Edges.*;
import org.jgrapht.traverse.DepthFirstIterator;
import org.jgraph.graph.DefaultEdge;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;

public class CGAnalyzer {
    private static boolean race = false;
    private static Stack<Node> AsyncStack = new Stack<Node>();
    private static Map<Node, Node> asyncToJoin = new HashMap<Node, Node>();

    public static boolean analyzeGraphForDataRace(DirectedAcyclicGraph<Node, DefaultEdge> graph) {
        CGAnalyzer.race = false;
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
        return CGAnalyzer.race;

    }

    /**
     * 
     * Checks the Current Node for outgoing/incoming edges, and updates the active set's validity
     * in relation to the isolation edge
     */
    private static Set<ValidatedSet> checkIsolatedNode(
                                               DirectedAcyclicGraph<Node,DefaultEdge> graph,
                                               Node currentNode,
                                               ValidatedSet activeSet,
                                               Set<ValidatedSet> parallelAccesses,
                                               Set<ValidatedSet> seriesAccesses)
    {
        Set<DefaultEdge> outgoingEdges = graph.outgoingEdgesOf(currentNode);   
        Node child = null;
        for (DefaultEdge edge : outgoingEdges) {
            if (edge.getAttributes().equals(IsolatedEdgeAttributes())) {
                System.out.println("Do the isolated edge thing"); 
                Node edgeTarget = graph.getEdgeTarget(edge);
                activeSet.setExpiresAfter(edgeTarget);
                activeSet = new ValidatedSet();
                seriesAccesses.add(activeSet);
            }
            else
            {
                if(child != null)
                {
                    throw new RuntimeException("Isolated Node has more than one child");
                }
                updateAsyncToJoin(graph, edge);
                child = graph.getEdgeTarget(edge);
            }
        }
        for (DefaultEdge edge : graph.incomingEdgesOf(currentNode)) {
            if (edge.getAttributes().equals(IsolatedEdgeAttributes())) {
                System.out.println("Do the other isolated edge thing"); 
                Node edgeSource = graph.getEdgeSource(edge);
                activeSet = new ValidatedSet();
                activeSet.setValidAfter(edgeSource);
                activeSet.setIsValid(false);
                seriesAccesses.add(activeSet);
            }
        }
        if(child == null)
        {
            throw new RuntimeException("Isolated node did not have a child, graph not ready to be checked");
        }
        return checkForDataRace(graph, child, activeSet, 
                                parallelAccesses, seriesAccesses);
    }

    private static Set<ValidatedSet> checkAsyncNode(DirectedAcyclicGraph<Node,DefaultEdge> graph,
                                                  Node currentNode,
                                                  ValidatedSet activeSet,
                                                  Set<ValidatedSet> parallelAccesses,
                                                  Set<ValidatedSet> seriesAccesses)
    {
        AsyncStack.push(currentNode); // add to global stack for linking to join
        Set<DefaultEdge> outgoingEdges = graph.outgoingEdgesOf(currentNode);
        Set<ValidatedSet> asyncParallel = null;

        return null;
    }
    /**
     * Update Globabl map to link Join nodes and Async nodes
     */
    private static void updateAsyncToJoin(DirectedAcyclicGraph graph, DefaultEdge edge)
    {
        if (edge.getAttributes().equals(JoinEdgeAttributes())) {
            //this code keeps track of connected asyncs and joins. Add this to
            //new standard SP-bags algorithm.
                if (AsyncStack.empty()) {
                    System.out.println("ERROR: Overdrawing Finish: " + graph.getEdgeTarget(edge));
                }
                else {
                    Node parentAsync = AsyncStack.pop();
                    Node joinTarget = (Node) graph.getEdgeTarget(edge);
                    System.out.println("Async to Join: " + parentAsync + " --> " + joinTarget);
                    asyncToJoin.put(parentAsync, joinTarget);
                }
        }
    }
    private static Set<ValidatedSet> checkForDataRace(
                                               DirectedAcyclicGraph<Node,DefaultEdge> graph,
                                               Node currentNode,
                                               ValidatedSet activeSet,
                                               Set<ValidatedSet> parallelAccesses,
                                               Set<ValidatedSet> seriesAccesses
                                               ) 
    {
        /*
         * If the current node is a Join, return the DFS seriesAccess, 
         */
        if(currentNode.isJoin())
        {
            return seriesAccesses;
        }

        /*
         * Report conflicts on current node, add reads/writes to activeSet
         */
        for (ValidatedSet vs : parallelAccesses)
        {
            vs.checkForConflicts(currentNode);
        }
        activeSet.addAllFrom(currentNode); 
        
        /*
         * If special node, call helper functions
         */
        if(currentNode.isAsync())
        {
            return checkAsyncNode(graph, currentNode, activeSet, 
                                  parallelAccesses, seriesAccesses);
        }
        if(currentNode.isIsolated())
        {
            return checkIsolatedNode(graph,currentNode, activeSet, 
                                     parallelAccesses, seriesAccesses);
        }
        
        Set<DefaultEdge> outgoingEdges = graph.outgoingEdgesOf(currentNode);
       
        //Check if the node has one child
        if (outgoingEdges.size() != 1)
        {
            throw new RuntimeException("Expected Node to have one child");
        }

        DefaultEdge edge = outgoingEdges.iterator().next();
        updateAsyncToJoin(graph, edge);
        Node child = graph.getEdgeTarget(edge);
        
        return checkForDataRace(graph, child, activeSet, 
                                parallelAccesses, seriesAccesses);
    }


    private static Set<ValidatedSet> union(Set<ValidatedSet> first, Set<ValidatedSet> second) {
        if (second != null) first.addAll(second);
        return first;
    }

    private static Node getRootOf(DirectedAcyclicGraph<Node, DefaultEdge> graph) {
        return graph.iterator().next();
    }
    
    private static class ValidatedSet {
        private Node expiresAfter;
        private Node validAfter;
        private boolean isValid;
        private boolean wasValidAtAsync;
        private Set<DataAccess> fieldRead;
        private Set<DataAccess> fieldWrite;
        private Set<DataAccess> arrayRead;
        private Set<DataAccess> arrayWrite;

        public Node getExpiresAfter()
        {
            return this.expiresAfter;
        }

        public void setExpiresAfter(Node expiresAfter)
        {
            this.expiresAfter = expiresAfter;
        }

        public Node getValidAfter()
        {
            return this.validAfter;
        }

        public void setValidAfter(Node validAfter)
        {
            this.validAfter = validAfter;
        }

        public void setIsValid(boolean isValid)
        {
            this.isValid = isValid;
        }

        public ValidatedSet() {
            this.expiresAfter = null;
            this.validAfter = null;
            this.isValid = true;
            this.fieldRead = new HashSet<DataAccess>();
            this.fieldWrite = new HashSet<DataAccess>();
            this.arrayRead = new HashSet<DataAccess>();
            this.arrayWrite = new HashSet<DataAccess>();
        }

        public ValidatedSet(
                            Set<DataAccess> fieldRead,
                            Set<DataAccess> fieldWrite,
                            Set<DataAccess> arrayRead,
                            Set<DataAccess> arrayWrite,
                            boolean isValid,
                            Node expiresAfter,
                            Node validAfter
                            ) {
            this.fieldRead = fieldRead;
            this.fieldWrite = fieldWrite;
            this.arrayRead = arrayRead;
            this.arrayWrite = arrayWrite;
            this.isValid = isValid;
            this.expiresAfter = expiresAfter;
            this.validAfter = validAfter;
        }

        public void checkForConflicts(Node input) {
            if (!(input instanceof activityNode)) return;
            activityNode node = (activityNode) input;

            for (DataAccess da : fieldRead) {
                
                if (node.var_write != null) for (DataAccess e : node.var_write) {
                    if(da.conflictsWith(e)) {
                        String message = "Non-deterministic access between\n\t"
                        + da.toString() + "\n\t" + e.toString();
                        reportRace(message);
                    }
                }
                if (node.isolated_write != null) for (DataAccess e : node.isolated_write) {
                    if(da.conflictsWith(e)) {
                        if (da.isIsolated()) {
                            String message = "Intended data race between\n\t"
                            + da.toString() + "\n\t" + e.toString();
                            System.out.println(message);

                        }
                        else {
                            String message = "Non-deterministic access between\n\t"
                            + da.toString() + "\n\t" + e.toString();
                            reportRace(message);
                        }
                    }
                }
            }
            for (DataAccess da : fieldWrite) {
                
                if (node.var_read != null) for (DataAccess e : node.var_read) {
                    if(da.conflictsWith(e)) {
                        String message = "Non-deterministic access between\n\t"
                        + da.toString() + "\n\t" + e.toString();
                        reportRace(message);
                    }
                }
                if (node.var_write != null) for (DataAccess e : node.var_write) {
                    if(da.conflictsWith(e)) {
                        String message = "Non-deterministic access between\n\t"
                        + da.toString() + "\n\t" + e.toString();
                        reportRace(message);
                    }
                }
                if (node.isolated_read != null) for (DataAccess e : node.isolated_read) {
                    if(da.conflictsWith(e)) {
                        if (da.isIsolated()) {
                            String message = "Intended data race between\n\t"
                            + da.toString() + "\n\t" + e.toString();
                            System.out.println(message);

                        }
                        else {
                            String message = "Non-deterministic access between\n\t"
                            + da.toString() + "\n\t" + e.toString();
                            reportRace(message);
                        }
                    }
                }
                if (node.isolated_write != null) for (DataAccess e : node.isolated_write) {
                    if(da.conflictsWith(e)) {
                        if (da.isIsolated()) {
                            String message = "Intended data race between\n\t"
                            + da.toString() + "\n\t" + e.toString();
                            System.out.println(message);

                        }
                        else {
                            String message = "Non-deterministic access between\n\t"
                            + da.toString() + "\n\t" + e.toString();
                            reportRace(message);
                        }
                    }
                }
            }

            for (DataAccess da : arrayRead) {
                
                if (node.array_write != null) for (DataAccess e : node.array_write) {
                    if(da.conflictsWith(e)) {
                        String message = "Non-deterministic access between\n\t"
                        + da.toString() + "\n\t" + e.toString();
                        reportRace(message);
                    }
                }
                if (node.array_write_isolated != null) for (DataAccess e : node.array_write_isolated) {
                    if(da.conflictsWith(e)) {
                        if (da.isIsolated()) {
                            String message = "Intended data race between\n\t"
                            + da.toString() + "\n\t" + e.toString();
                            System.out.println(message);

                        }
                        else {
                            String message = "Non-deterministic access between\n\t"
                            + da.toString() + "\n\t" + e.toString();
                            reportRace(message);
                        }
                    }
                }
            }

            for (DataAccess da : arrayWrite) {
                
                if (node.array_read != null) for (DataAccess e : node.array_read) {
                    if(da.conflictsWith(e)) {
                        String message = "Non-deterministic access between\n\t"
                        + da.toString() + "\n\t" + e.toString();
                        reportRace(message);
                    }
                }
                if (node.array_write != null) for (DataAccess e : node.array_write) {
                    if(da.conflictsWith(e)) {
                        String message = "Non-deterministic access between\n\t"
                        + da.toString() + "\n\t" + e.toString();
                        reportRace(message);
                    }
                }
                if (node.array_read_isolated != null) for (DataAccess e : node.array_read_isolated) {
                    if(da.conflictsWith(e)) {
                        if (da.isIsolated()) {
                            String message = "Intended data race between\n\t"
                            + da.toString() + "\n\t" + e.toString();
                            System.out.println(message);

                        }
                        else {
                            String message = "Non-deterministic access between\n\t"
                            + da.toString() + "\n\t" + e.toString();
                            reportRace(message);
                        }
                    }
                }
                if (node.array_write_isolated != null) for (DataAccess e : node.array_write_isolated) {
                    if(da.conflictsWith(e)) {
                        if (da.isIsolated()) {
                            String message = "Intended data race between\n\t"
                            + da.toString() + "\n\t" + e.toString();
                            System.out.println(message);

                        }
                        else {
                            String message = "Non-deterministic access between\n\t"
                            + da.toString() + "\n\t" + e.toString();
                            reportRace(message);
                        }
                    }
                }
            }

        }

        public void addAllFrom(Node input) {
            if (!(input instanceof activityNode)) return;
            activityNode node = (activityNode) input;

            if (node.var_read != null) for (DataAccess e : node.var_read) {
                this.fieldRead.add(e);
            }
            if (node.var_write != null) for (DataAccess e : node.var_write) {
                this.fieldWrite.add(e);
            }
            if (node.isolated_read != null) for (DataAccess e : node.isolated_read) {
                e.setIsolated(true);
                this.fieldRead.add(e);
            }
            if (node.isolated_write != null) for (DataAccess e : node.isolated_write) {
                e.setIsolated(true);
                this.fieldWrite.add(e);
            }
            if (node.array_read != null) for (DataAccess e : node.array_read) {
                this.arrayRead.add(e);
            }
            if (node.array_write != null) for (DataAccess e : node.array_write) {
                this.arrayWrite.add(e);
            }
            if (node.array_read_isolated != null) for (DataAccess e : node.array_read_isolated) {
                e.setIsolated(true);
                this.arrayRead.add(e);
            }
            if (node.array_write_isolated != null) for (DataAccess e : node.array_write_isolated) {
                e.setIsolated(true);
                this.arrayWrite.add(e);
            }
        }

        boolean isValidAt(Node currentNode) {
            if (currentNode.isAsync()) {
                this.wasValidAtAsync = isValid;
            }
            if (this.expiresAfter != null) {
                if (currentNode == this.expiresAfter) this.isValid = false;
            }
            else if (this.validAfter != null) {
                if (currentNode == this.validAfter) this.isValid = true;
            }
            else {
                this.isValid = true;
            }
            return this.isValid;
        }
    }

    private static void reportRace(String message) {
        CGAnalyzer.race = true;
        System.out.println(message);
    }


}
