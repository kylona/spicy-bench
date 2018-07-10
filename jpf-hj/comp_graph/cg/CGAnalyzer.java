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

    private static Set<ValidatedSet> checkForDataRace(
                                               DirectedAcyclicGraph<Node,DefaultEdge> graph,
                                               Node currentNode,
                                               ValidatedSet activeSet,
                                               Set<ValidatedSet> parallelAccesses,
                                               Set<ValidatedSet> seriesAccesses
                                               ) {
        if(currentNode.isJoin()) {
            return seriesAccesses;
        }
        if(currentNode.isAsync()) {
            AsyncStack.push(currentNode);
        }
        for (ValidatedSet vs : parallelAccesses) {
            vs.checkForConflicts(currentNode); 
        }
        activeSet.addAllFrom(currentNode);

        Set<DefaultEdge> outgoingEdges = graph.outgoingEdgesOf(currentNode);
        Set<ValidatedSet> asyncParallel = null;
        for (DefaultEdge edge : outgoingEdges) {
            //Check for edge types and do stuff
            if (edge.getAttributes().equals(JoinEdgeAttributes())) {
                if (AsyncStack.empty()) {
                    System.out.println("ERROR: Overdrawing Finish: " + currentNode);
                }
                else {
                    Node parentAsync = AsyncStack.pop();
                    Node joinTarget = graph.getEdgeTarget(edge);
                    System.out.println("Async to Join: " + parentAsync + " --> " + joinTarget);
                }
            }
            if (edge.getAttributes().equals(IsolatedEdgeAttributes())) {
                System.out.println("Skipping Isolated Edge: " +
                graph.getEdgeSource(edge) + " --> " + graph.getEdgeTarget(edge)); 
                continue;
            }
            //Edge stuff done.

            if (currentNode.isAsync()) {
                Node child = (Node) graph.getEdgeTarget(edge);
                if (asyncParallel == null) { //first child
                    Set<ValidatedSet> asyncSeries = new HashSet<ValidatedSet>();
                    ValidatedSet newActiveSet = new ValidatedSet();
                    asyncSeries.add(newActiveSet);

                    Set<ValidatedSet> result =
                    checkForDataRace(graph,child,newActiveSet,parallelAccesses,asyncSeries);

                    asyncParallel = new HashSet<ValidatedSet>();
                    asyncParallel = union(asyncParallel, result);
                    seriesAccesses = union(seriesAccesses, result);
                }
                else { //second+ child
                    Set<ValidatedSet> asyncSeries = new HashSet<ValidatedSet>();
                    ValidatedSet newActiveSet = new ValidatedSet();
                    asyncSeries.add(newActiveSet);

                    Set<ValidatedSet> result =
                    checkForDataRace(graph,child,activeSet,asyncParallel,asyncSeries);
                    union(asyncParallel, result);
                    union(seriesAccesses, result);
                }
            }
            else {
                if (outgoingEdges.size() != 1) throw new RuntimeException("ERROR: skipped edge");
                Node child = (Node) graph.getEdgeTarget(edge);
                Set<ValidatedSet> result =
                checkForDataRace(graph,child,activeSet,parallelAccesses,seriesAccesses);
            }
        }
        return seriesAccesses;
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
                            reportRace(message);

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
                            reportRace(message);

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
                            reportRace(message);

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
                            reportRace(message);

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
                            reportRace(message);

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
                            reportRace(message);

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
