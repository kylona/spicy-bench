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
import static edu.rice.hj.Module2.isolated;
import edu.rice.hj.api.HjRunnable;
import edu.rice.hj.api.HjSuspendable;

/**
 * bfsDijkstra implements the breadth first algorithm using the Dijkstra
 * approach. The aim here is to construct a tree that marks the parent child
 * relationship between two nodes.
 *
 * @author Peter Anderson <anderson.peter@byu.edu> (minor edits to update to
 * hj-lib)
 * @author Suyash Gupta
 * @author V Krishna Nandivada
 */
public class bfsDijkstraNode {

    final int START = 0;
    final int JOIN = 1;
    final int ACK = 2;
    final int NACK = 3;
    int nodes, root, Infinity = Integer.MAX_VALUE, cPhase = 0;

    /**
     * Parameters to enable execution with load
     */
    long loadValue = 0, nval[];
    boolean found[];
    int adj_graph[][] = {
        {0, 1, 0, 0, 0, 0, 0, 0},
        {1, 0, 1, 0, 0, 0, 0, 0},
        {0, 1, 0, 1, 0, 0, 0, 0},
        {0, 0, 1, 0, 1, 0, 0, 0},
        {0, 0, 0, 1, 0, 1, 0, 0},
        {0, 0, 0, 0, 1, 0, 1, 0},
        {0, 0, 0, 0, 0, 1, 0, 1},
        {0, 0, 0, 0, 0, 0, 1, 0}
    };

    /**
     * Abstract node representation
     */
    BFSDijkstraNode nodeSet[];

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
                bfsDijkstraNode bfs = new bfsDijkstraNode();

                bfs.root = 5;
                bfs.nodes = 8;

                bfs.found = new boolean[bfs.nodes];
                bfs.nodeSet = new BFSDijkstraNode[bfs.nodes];
                bfs.nval = new long[bfs.nodes];
                bfs.initialize();
                boolean newNode = false;
                while (true) {
                    bfs.found = new boolean[bfs.nodes];
                    bfs.nodeSet[bfs.root].currentPhase = bfs.cPhase;
                    bfs.nodeSet[bfs.root].signal = bfs.START;

                    bfs.broadCast(bfs.root, bfs.cPhase);
                    bfs.joinMessage();
                    bfs.joinTree();
                    newNode = bfs.foundCheck();
                    if (!newNode) {
                        break;
                    }
                    bfs.echoReply(bfs.root);
                    bfs.reset();
                    bfs.cPhase++;
                }
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
                            nodeSet[k] = new BFSDijkstraNode();
                            nodeSet[k].parent = Infinity;
                            nodeSet[k].phaseDiscovered = -1;
                            nodeSet[k].currentPhase = 0;

                            if (k == root) {
                                nodeSet[k].parent = 0;
                                nodeSet[k].phaseDiscovered = 0;
                            }
                        }
                    });
                }
            }
        });
    }

    /**
     * Aims to busy the threads by introducing the no-op instructions equivalent
     * to the amount of load specified.
     *
     * @param weight	Specifies the current load value for a thread.
     * @return Updated load value.
     */
    long loadweight(long weight) {
        long j = 0;
        for (long i = 0; i < loadValue; i++) {
            j++;
        }
        return j + weight;
    }

    /**
     * Transmits the <code>START<\code> signal to all the children of a node.
     */
    void broadCast(int aNode, int phase) {
        final int f_aNode = aNode;
        final int f_phase = phase;
        finish(new HjSuspendable() {
            public void run() {
                for (int i = 0; i < nodeSet[f_aNode].children.size(); i++) {
                    final int k = i;
                    async(new HjRunnable() {
                        public void run() {
                            sendSignal(nodeSet[f_aNode].children.get(k), START, f_phase);
                            broadCast(nodeSet[f_aNode].children.get(k), f_phase);

                            if (loadValue != 0) {
                                nval[k] = loadweight(nval[k] + k);
                            }
                        }
                    });
                }
            }
        });
    }

    /**
     * Transmits the join message to all the undiscovered neighbors of the node
     * discovered in previous phase.
     */
    void joinMessage() {
        finish(new HjSuspendable() {
            public void run() {
                for (int i = 0; i < nodes; i++) {
                    final int f_i = i;
                    async(new HjRunnable() {
                        public void run() {
                            if (nodeSet[f_i].phaseDiscovered == nodeSet[f_i].currentPhase) {
                                for (int j = 0; j < nodes; j++) {
                                    boolean flag = false;
                                    if (adj_graph[f_i][j] == 1) {
                                        for (int k = 0; k < nodeSet[f_i].neighborTalked.size(); k++) {
                                            if (nodeSet[f_i].neighborTalked.get(k) == j) {
                                                flag = true;
                                                break;
                                            }
                                        }
                                        if (!flag) {
                                            nodeSet[f_i].neighborTalked.add(j);
                                            sendJoinMessage(j, f_i, JOIN, nodeSet[f_i].currentPhase + 1);
                                        }
                                    }
                                }
                            }

                            if (loadValue != 0) {
                                nval[f_i] = loadweight(nval[f_i] + f_i);
                            }
                        }
                    });
                }
            }
        });
    }

    /**
     * Merges new nodes to existing BFS Tree.
     */
    void joinTree() {
        finish(new HjSuspendable() {
            public void run() {
                for (int i = 0; i < nodes; i++) {
                    final int f_i = i;
                    async(new HjRunnable() {
                        public void run() {
                            if (nodeSet[f_i].parent == Infinity) {
                                messagePair msp = new messagePair();
                                boolean flag = false;
                                for (int j = 0; j < nodeSet[f_i].sendMessage.size(); j++) {
                                    msp = nodeSet[f_i].sendMessage.get(j);
                                    if (!flag) {
                                        sendAck(msp.from, f_i, ACK);
                                        nodeSet[f_i].parent = msp.from;
                                        nodeSet[f_i].neighborTalked.add(msp.from);
                                        setChild(msp.from, f_i);
                                        found[f_i] = true;
                                        flag = true;
                                    } else {
                                        nodeSet[f_i].neighborTalked.add(msp.from);
                                        sendAck(msp.from, f_i, NACK);
                                    }
                                }
                            } else {
                                messagePair msp = new messagePair();
                                for (int j = 0; j < nodeSet[f_i].sendMessage.size(); j++) {
                                    msp = nodeSet[f_i].sendMessage.get(j);
                                    sendAck(msp.from, f_i, NACK);
                                    nodeSet[f_i].neighborTalked.add(msp.from);
                                }
                            }

                            if (loadValue != 0) {
                                nval[f_i] = loadweight(nval[f_i] + f_i);
                            }
                        }
                    });
                }
            }
        });

        finish(new HjSuspendable() {
            public void run() {
                for (int i = 0; i < nodes; i++) {
                    final int f_i = i;
                    async(new HjRunnable() {
                        public void run() {
                            nodeSet[f_i].sendMessage.clear();
                            nodeSet[f_i].sendMessage.addAll(nodeSet[f_i].tempHolder);
                            nodeSet[f_i].tempHolder.clear();

                            if (loadValue != 0) {
                                nval[f_i] = loadweight(nval[f_i] + f_i);
                            }
                        }
                    });
                }
            }
        });
    }

    /**
     * Tells whether all the nodes are part of the BFS tree or some nodes are
     * left.
     *
     * @return	true if all nodes are part of the BFS tree.
     */
    boolean foundCheck() {
        int counter = 0;
        boolean flag = false;
        for (int i = 0; i < nodes; i++) {
            if (found[i]) {
                counter++;
                break;
            }
        }

        if (counter > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Replies are echoed back to root from all the children.
     *
     * @param aNode	Node which echoes reply back to its parent.
     */
    void echoReply(int aNode) {
        final int f_aNode = aNode;
        if (nodeSet[aNode].phaseDiscovered != nodeSet[aNode].currentPhase) {
            finish(new HjSuspendable() {
                public void run() {
                    for (int i = 0; i < nodeSet[f_aNode].children.size(); i++) {
                        final int f_i = i;
                        async(new HjRunnable() {
                            public void run() {
                                echoReply(nodeSet[f_aNode].children.get(f_i));
                                if (loadValue != 0) {
                                    nval[f_i] = loadweight(nval[f_i] + f_i);
                                }
                            }
                        });
                    }
                }
            });
            sendAck(nodeSet[f_aNode].parent, f_aNode, ACK);
        }
    }

    /**
     * Resets the variables.
     */
    void reset() {
        finish(new HjSuspendable() {
            public void run() {
                for (int i = 0; i < nodes; i++) {
                    final int f_i = i;
                    async(new HjRunnable() {
                        public void run() {
                            nodeSet[f_i].sendMessage = new ArrayList<messagePair>();
                            nodeSet[f_i].signal = -1;

                            if (loadValue != 0) {
                                nval[f_i] = loadweight(nval[f_i] + f_i);
                            }
                        }
                    });
                }
            }
        });
    }

    /**
     * Sets a node as the child node of another node.
     *
     * @param aNode	Parent Node.
     * @param uNode	Child Node.
     */
    void setChild(int aNode, int uNode) {
        final int f_aNode = aNode;
        final int f_uNode = uNode;
        isolated(new HjRunnable() {
            public void run() {
                nodeSet[f_aNode].children.add(f_uNode);
            }
        });
    }

    /**
     * Sends <code>ACK<\code> or <code>NACK<\code> signal to the sender of
     * <code>JOIN<\code> signal.
     *
     * @param aNode	Signal receiver.
     * @param uNode	Signal sender.
     * @param uSignal	<code>ACK<\code> or <code>NACK<\code> signal.
     */
    void sendAck(int aNode, int uNode, int uSignal) {
        final int f_aNode = aNode;
        final messagePair msp = new messagePair();
        msp.from = uNode;
        msp.signal = uSignal;
        isolated(new HjRunnable() {
            public void run() {
                nodeSet[f_aNode].tempHolder.add(msp);
            }
        });
    }

    /**
     * Sends <code>JOIN<\code> signal to all the prospective children.
     *
     * @param aNode	Signal receiver.
     * @param from	Signal sender.
     * @param signal	<code>JOIN<\code> signal.
     * @param phase	Current phase value.
     */
    void sendJoinMessage(int aNode, int from, int signal, int phase) {
        final int f_aNode = aNode;
        final int f_from = from;
        final int f_phase = phase;
        isolated(new HjRunnable() {
            public void run() {
                messagePair msp = new messagePair();
                msp.from = f_from;
                msp.signal = JOIN;
                nodeSet[f_aNode].sendMessage.add(msp);

                if (nodeSet[f_aNode].parent == Infinity) {
                    nodeSet[f_aNode].phaseDiscovered = f_phase;
                }
            }
        });
    }

    /**
     * Sends <code>START<\code> signal to a node.
     *
     * @param aNode	Signal receiver.
     * @param uSignal	<code>START<\code> signal.
     * @param phase	Current phase value.
     */
    void sendSignal(int aNode, int uSignal, int phase) {
        nodeSet[aNode].signal = uSignal;
        nodeSet[aNode].currentPhase = phase;
    }

    /**
     * Validates the output resulting from the execution of the algorithm.
     */
    void outputVerifier() {
        int i;
        int nodeCheck[] = new int[nodes];
        boolean flag = false;

        for (i = 0; i < nodes; i++) {
            if (nodeSet[i].children.size() > 0) {
                for (int j = 0; j < nodeSet[i].children.size(); j++) {
                    nodeCheck[nodeSet[i].children.get(j)]++;
                }
            }
        }
        nodeCheck[root]++;
        for (i = 0; i < nodes; i++) {
            if (nodeCheck[i] != 1) {
                flag = true;
            }
        }

        if (!flag) {
            System.out.println("Output verified");
        }
    }
}

/**
 * States the structure of message to be transmitted.
 */
class messagePair {

    int from;
    int signal;
}

/**
 * <code>Node</code> specifies the structure for each abstract node part of the
 * BFS Dijkstra algorithm.
 */
class BFSDijkstraNode {

    /**
     * Specifies parent of a node.
     */
    int parent;

    /**
     * Specifies the phase in which the node is discovered.
     */
    int phaseDiscovered;

    /**
     * Specifies the current ongoing phase.
     */
    int currentPhase;

    /**
     * Enumerates the set of children of the node.
     */
    ArrayList<Integer> children = new ArrayList<>();

    /**
     * Enumerates the neighbor communicated by a node.
     */
    ArrayList<Integer> neighborTalked = new ArrayList<>();

    /**
     * Holds all the messages received by a node.
     */
    ArrayList<messagePair> sendMessage = new ArrayList<>();
    ArrayList<messagePair> tempHolder = new ArrayList<messagePair>();

    int signal;
}
