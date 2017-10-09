package benchmarks.IMSuite;

/* This file is part of IMSuite Benchamark Suite.
 * 
 * This file is licensed to You under the Eclipse Public License (EPL);
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.opensource.org/licenses/eclipse-1.0.php
 *
 * (C) Copyright IMSuite 2013-present.
 */
import java.util.*;
import static edu.rice.hj.Module1.async;
import static edu.rice.hj.Module1.finish;
import static edu.rice.hj.Module1.launchHabaneroApp;
import edu.rice.hj.api.HjRunnable;
import edu.rice.hj.api.HjSuspendable;
import static permission.PermissionChecks.*;

/**
 * dijkstraRouting aims to create a routing table specific for each node,
 * consistingNodes can be classified as traitors and non-traitors. Traitors aim
 * to disrupt the consensus.
 *
 * @author Peter Anderson <anderson.peter@byu.edu> (minor edits to update to
 * hj-lib)
 * @author Suyash Gupta
 * @author V Krishna Nandivada
 */
public class dijkstraRouting {

    int nodes, Infinity = Integer.MAX_VALUE;

    /**
     * Parameters to enable execution with load
     */
    long loadValue = 0, nval[];

    /**
     * Abstract node representation
     */
    DijkstraRoutingNode nodeSet[];

    int[][] adj_graph = {
        {0, 1, 1, 1, 1, 1, 1, 1},
        {1, 0, 1, 0, 1, 1, 1, 1},
        {1, 1, 0, 1, 1, 0, 0, 0},
        {1, 0, 1, 0, 1, 0, 1, 1},
        {1, 1, 1, 1, 0, 1, 1, 1},
        {1, 1, 0, 0, 1, 0, 1, 1},
        {1, 1, 0, 1, 1, 1, 0, 1},
        {1, 1, 0, 1, 1, 1, 1, 0}
    };

    int[][] weightMatrix = {
        {2_147_483_647, 1, 1, 1, 1, 1, 1, 1},
        {2_147_483_647, 1, 1, 1, 1, 1, 1, 1},
        {2_147_483_647, 1, 1, 1, 1, 1, 1, 1},
        {2_147_483_647, 1, 1, 1, 1, 1, 1, 1},
        {2_147_483_647, 1, 1, 1, 1, 1, 1, 1},
        {2_147_483_647, 1, 1, 1, 1, 1, 1, 1},
        {2_147_483_647, 1, 1, 1, 1, 1, 1, 1},
        {2_147_483_647, 1, 1, 1, 1, 1, 1, 1},};

    /**
     * Acts as the starting point for the program execution. <code>main</code>
     * performs the task of accepting the input from the user specified file,
     * calling the methods responsible for BFS tree creation, printing the
     * output and validating the result.
     *
     * @param args array of runtime arguments.
     * @throws Exception	if File handling operation illegal.
     */
    public static void main(final String[] args) throws Exception {
        launchHabaneroApp(new HjSuspendable() {
            public void run() {
                dijkstraRouting djr = new dijkstraRouting();
                djr.nodes = 8;
                djr.nodeSet = new DijkstraRoutingNode[djr.nodes];
                djr.nval = new long[djr.nodes];

                djr.initialize();
                djr.route();
            }
        });
    }

    /**
     * Initializes all the fields of the abstract node.
     */
    void initialize() {
        finish(new HjSuspendable() {
            public void run() {
                for (int i = 0; i < nodes; i++) {
                    final int f_i = i;
                    async(new HjRunnable() {
                        public void run() {
                            int j, count = 0;
                            nodeSet[f_i] = new DijkstraRoutingNode();
                            nodeSet[f_i].id = f_i;
                            nodeSet[f_i].routingTable = new routingInfo[nodes];
                            int[] row = adj_graph[f_i];
                            acquireR(row, 0, nodes - 1);
                            for (j = 0; j < nodes; j++) {
                                nodeSet[f_i].routingTable[j] = new routingInfo();
                                if (adj_graph[f_i][j] == 1) {
                                    count++;
                                }
                            }
                            releaseR(row, 0, nodes - 1);

                            nodeSet[f_i].neighbors = new int[count];
                            count = 0;
                            acquireR(row, 0, nodes - 1);
                            for (j = 0; j < nodes; j++) {
                                if (adj_graph[f_i][j] == 1) {
                                    nodeSet[f_i].neighbors[count] = j;
                                    count++;
                                }
                            }
                            releaseR(row, 0, nodes - 1);
                        }
                    });
                }
            }
        });
    }

    /**
     * Generates the routing table for each node.
     */
    void route() {
        finish(new HjSuspendable() {
            public void run() {
                for (int i = 0; i < nodes; i++) {
                    final int f_i = i;
                    async(new HjRunnable() {
                        public void run() {
                            int j, minCost, node = -1, hCount = 0, nrt = 0, maxHop = 0;
                            final int parent[] = new int[nodes];
                            final boolean notLeaf[] = new boolean[nodes];
                            boolean nodeCovered[] = new boolean[nodes];
                            ArrayList<routingInfo> queue = new ArrayList<routingInfo>();

                            routingInfo rtf = new routingInfo();
                            parent[f_i] = Infinity;
                            rtf.setVlue(f_i, 0, 0, f_i);
                            queue.add(rtf);

                            while (queue.size() > 0) {
                                minCost = Infinity;
                                for (j = queue.size() - 1; j >= 0; j--) {
                                    rtf = queue.get(j);
                                    if (minCost > rtf.costToReach) {
                                        minCost = rtf.costToReach;
                                        node = rtf.nodeId;
                                        hCount = rtf.hopCount;
                                        nrt = rtf.nextNode;
                                    }
                                }

                                rtf = new routingInfo();
                                rtf.setVlue(node, minCost, hCount, nrt);
                                if (node != f_i) {
                                    parent[node] = nrt;
                                    notLeaf[parent[node]] = true;
                                }
                                acquireW(nodeSet, f_i);
                                nodeSet[f_i].routingTable[node] = rtf;
                                releaseW(nodeSet, f_i);
                                nodeCovered[node] = true;
                                for (j = queue.size() - 1; j >= 0; j--) {
                                    rtf = queue.get(j);
                                    if (rtf.nodeId == node) {
                                        queue.remove(j);
                                    }
                                }

                                int sz = nodeSet[node].neighbors.length;
                                int neighborSet[] = new int[sz];
                                neighborSet = nodeSet[node].neighbors;
                                for (j = 0; j < sz; j++) {
                                    if (!nodeCovered[neighborSet[j]]) {
                                        rtf = new routingInfo();
                                        rtf.setVlue(neighborSet[j], minCost + weightMatrix[node][neighborSet[j]], hCount + 1, node);
                                        queue.add(rtf);
                                    }
                                }
                            }

                            finish(new HjSuspendable() {
                                public void run() {
                                    for (int k = 0; k < nodeSet[f_i].neighbors.length; k++) {
                                        final int f_k = k;
                                        async(new HjRunnable() {
                                            public void run() {
                                                if (parent[nodeSet[f_i].neighbors[f_k]] == f_i) {
                                                    neighborTask(f_i, nodeSet[f_i].neighbors[f_k], parent, notLeaf);
                                                    nodeSet[f_i].routingTable[nodeSet[f_i].neighbors[f_k]].nextNode = f_i;
                                                }
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    });
                }
            }
        });
    }

    /**
     * Associates each neighbor of a node to provide the next node information.
     *
     * @param sender	node whose routing table has to be updated
     * @param aNode	neighbor of <code>sender<\code> which needs to update
     * information
     * @param parent	information about parent of a node according to
     * <code>sender<\code>
     * @param notLeaf	tells which node is leaf or not.
     */
    void neighborTask(int sender, int aNode, int parent[], boolean notLeaf[]) {
        for (int k = 0; k < nodeSet[aNode].neighbors.length; k++) {
            if (parent[nodeSet[aNode].neighbors[k]] == aNode) {
                findNextNode(sender, aNode, nodeSet[aNode].neighbors[k], parent, notLeaf);
            }
        }
    }

    /**
     * Provides the next node information.
     *
     * @param pNode	node whose routing table has to be updated
     * @param uNode	node which acts as the next node information
     * @param aNode	node whose entry in <code>pNode<\code> routing table has to
     * be updated
     * @param parent	information about parent of a node according to
     * <code>sender<\code>
     * @param notLeaf	tells which node is leaf or not.
     */
    void findNextNode(int pNode, int uNode, int aNode, final int parent[], final boolean notLeaf[]) {
        final int f_aNode = aNode;
        final int f_uNode = uNode;
        final int f_pNode = pNode;
        if (!notLeaf[aNode]) {
            nodeSet[pNode].routingTable[aNode].nextNode = uNode;
            return;
        } else {
            nodeSet[pNode].routingTable[aNode].nextNode = uNode;
            finish(new HjSuspendable() {
                public void run() {
                    for (int j = 0; j < nodeSet[f_aNode].neighbors.length; j++) {
                        final int f_j = j;
                        async(new HjRunnable() {
                            public void run() {
                                if (parent[nodeSet[f_aNode].neighbors[f_j]] == f_aNode) {
                                    findNextNode(f_pNode, f_uNode, nodeSet[f_aNode].neighbors[f_j], parent, notLeaf);
                                }
                            }
                        });
                    }
                }
            });
        }
    }
}

/**
 * States the structure for the routing table.
 */
class routingInfo {

    /**
     * Specifies identifier of the destination node.
     */
    int nodeId;

    /**
     * Specifies the hops needed to reach the destination node.
     */
    int hopCount;

    /**
     * Specifies the cost of the path to reach source.
     */
    int costToReach;

    /**
     * Identifies the first node on path to destination node from source.
     */
    int nextNode;

    void setVlue(int nodeId, int costToReach, int hopCount, int nextNode) {
        this.nodeId = nodeId;
        this.costToReach = costToReach;
        this.hopCount = hopCount;
        this.nextNode = nextNode;
    }
}

/**
 * <code>Node</code> specifies the structure for each abstract node part of the
 * Dijkstra Routing algorithm.
 */
class DijkstraRoutingNode {

    /**
     * Specifies identifier of the source router.
     */
    int id;

    /**
     * Specifies neighbors of the router.
     */
    int neighbors[];

    /**
     * Routing table structure.
     */
    routingInfo routingTable[];

    /**
     * Holder for receiving the messages.
     */
    ArrayList<routingInfo> messageHolder = new ArrayList<>();
}
