/* This file is part of IMSuite Benchamark Suite.
 * 
 * This file is licensed to You under the Eclipse Public License (EPL);
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.opensource.org/licenses/eclipse-1.0.php
 *
 * (C) Copyright IMSuite 2013-present.
 */
package benchmarks.IMSuite;

import static edu.rice.hj.Module1.async;
import static edu.rice.hj.Module1.finish;
import static edu.rice.hj.Module1.launchHabaneroApp;
import static edu.rice.hj.Module2.isolated;
import static permission.PermissionChecks.*;
import edu.rice.hj.api.HjRunnable;
import edu.rice.hj.api.HjSuspendable;

/**
 *
 * @author Peter Anderson <anderson.peter@byu.edu> (minor edits to update to
 * hj-lib)
 * @author Suyash Gupta
 * @author V Krishna Nandivada
 */
public class bfsBellmanFord {

    int nodes, root, Infinity = Integer.MAX_VALUE;
    int diameter;
//    int[][] adj_graph = {{0, 1, 1, 1, 1, 1, 1, 1},
//    {1, 0, 1, 0, 1, 1, 1, 1},
//    {1, 1, 0, 1, 1, 0, 0, 0},
//    {1, 0, 1, 0, 1, 0, 1, 1},
//    {1, 1, 1, 1, 0, 1, 1, 1},
//    {1, 1, 0, 0, 1, 0, 1, 1},
//    {1, 1, 0, 1, 1, 1, 0, 1},
//    {1, 1, 0, 1, 1, 1, 1, 0}};

    int[][] adj_graph = {
        {1, 1, 1},
        {1, 1, 1},
        {1, 1, 1}
    };

    /**
     * Parameters to enable execution with load
     */
    long nval[];

    /**
     * Abstract node representation
     */
    BFSBellmanNode nodeSet[];

    /**
     * Acts as the starting point for the program execution. <code>main</code>
     * performs the task of accepting the input from the user specified file,
     * calling the <code>bfsForm</code>, printing the output and validating the
     * result.
     *
     * @param args array of runtime arguments.
     * @throws Exception if File handling operation illegal.
     */
    public static void main(final String[] args) throws Exception {
        launchHabaneroApp(new HjSuspendable() {
            public void run() {
                bfsBellmanFord bfs = new bfsBellmanFord();
                bfs.nodes = 3;
                bfs.root = 1;
                bfs.nval = new long[bfs.nodes];
                bfs.nodeSet = new BFSBellmanNode[bfs.nodes];
                bfs.initialize();
                bfs.bfsForm(bfs.root);
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
                    final int k = i;
                    async(new HjRunnable() {
                        public void run() {
                            int j, counter = 0;
                            nodeSet[k] = new BFSBellmanNode();
                            nodeSet[k].visited = false;
                            int[] row = adj_graph[k];
                            acquireR(row, 0, nodes - 1);
                            for (j = 0; j < nodes; j++) {
                                if (adj_graph[k][j] == 1) {
                                    counter++;
                                }
                            }
                            releaseR(row, 0, nodes - 1);
                            nodeSet[k].neighbors = new int[counter];
                            counter = 0;
                            for (j = 0; j < nodes; j++) {
                                if (adj_graph[k][j] == 1) {
                                    nodeSet[k].neighbors[counter] = j;
                                    counter++;
                                }
                            }
                            if (k != root) {
                                nodeSet[k].distance = Infinity;
                            } else {
                                nodeSet[k].distance = 0;
                            }
                        }
                    });
                }
            }
        });
    }

    /**
     * Performs the task of creating BFS. Each node sends its value of distance
     * from the root to all its neighbors. A node updates its distance variable
     * only if it receives a value smaller than the existing.
     */
    void bfsForm(int aNode) {
        final boolean[] flags = {false};
        final int f_aNode = aNode;
        isolated(() -> {
            if (!nodeSet[f_aNode].visited) {
                flags[0] = true;
            }
        });

        acquireR(flags, 0);
        if (flags[0]) {
            releaseR(flags, 0);
            isolated(new HjRunnable() {
                public void run() {
                    nodeSet[f_aNode].visited = true;
                }
            });

            finish(new HjSuspendable() {
                public void run() {
                    for (int i = 0; i < nodeSet[f_aNode].neighbors.length; i++) {
                        final int k = i;
                        async(new HjRunnable() {
                            public void run() {
                                acquireR(nodeSet, f_aNode);
                                acquireR(nodeSet[f_aNode].neighbors, k);
                                setDistance(nodeSet[f_aNode].neighbors[k], nodeSet[f_aNode].distance);
                                releaseR(nodeSet, f_aNode);
                                releaseR(nodeSet[f_aNode].neighbors, k);
                            }
                        });
                    }
                }
            });

            finish(new HjSuspendable() {
                public void run() {
                    for (int i = 0; i < nodeSet[f_aNode].neighbors.length; i++) {
                        final int k = i;
                        async(new HjRunnable() {
                            public void run() {
                                bfsForm(nodeSet[f_aNode].neighbors[k]);
                            }
                        });
                    }
                }
            });
        } else {
            releaseR(flags, 0);
        }
    }

    /**
     * Sends the distance value to a neighbor.
     *
     * @param neighbor Receiving node.
     * @param distance Value to be sent.
     */
    void setDistance(int neighbor, int distance) {
        final int f_neighbor = neighbor;
        final int f_distance = distance;
        isolated(new HjRunnable() {
            public void run() {
                if (nodeSet[f_neighbor].distance > f_distance) {
                    nodeSet[f_neighbor].distance = f_distance + 1;
                    nodeSet[f_neighbor].visited = false;
                }
            }
        });
    }
}

/**
 * <code>Node</code> specifies the structure for each abstract node part of the
 * BFS Bellman Ford algorithm.
 */
class BFSBellmanNode {

    /**
     * Tells distance of a node from the root.
     */
    int distance;

    /**
     * Tracks all the neighbors of a node.
     */
    int neighbors[];

    /**
     * Specifies if a node has already been visited or not.
     */
    boolean visited;
}
