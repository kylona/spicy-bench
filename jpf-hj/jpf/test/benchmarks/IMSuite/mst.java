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
 * mst aims to find a minimum spanning tree from a given graph consisting of a
 * set of nodes and edges. The main intuition behind the algorithm is selection
 * of a blue edge (minimum weighted edge joining a fragment with another
 * fragment).
 *
 * @author Suyash Gupta
 * @author V Krishna Nandivada
 */
public class mst {

    /**
     * Signals to indicate.
     */
    int START = 0;
    int JOIN = 1;
    int CHANGE = 2;

    int nodes;
    int Infinity = Integer.MAX_VALUE;
    long loadValue = 0, nval[];

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

    int[][] weight_matrix = {
        {2147483647, 1499111839, 1241509321, 767585128,
            250805619, 327805445, 1788893725, 1484984951},
        {1499111839, 2147483647, 1465436471, 1503359244,
            1547224697, 871803328, 422662934, 1549142155},
        {1241509321, 1465436471, 2147483647, 1089908931,
            1501139659, 1187124189, 819275505, 1433320589},
        {767585128, 1503359244, 1089908931, 2147483647,
            1184083145, 709920380, 1509617579, 133216726},
        {250805619, 1547224697, 1501139659, 1184083145,
            2147483647, 667821178, 334457144, 11401960},
        {327805445, 871803328, 1187124189, 709920380,
            667821178, 2147483647, 870504536, 365593649},
        {1788893725, 422662934, 819275505, 1509617579,
            334457144, 870504536, 2147483647, 435764790},
        {1484984951, 1549142155, 1433320589, 133216726,
            11401960, 365593649, 435764790, 2147483647}
    };

    /**
     * Abstract node representation.
     */
    MSTNode nodeSet[];

    boolean flag[];

    /**
     * Acts as the starting point for the program execution. <code>main</code>
     * performs the task of accepting the input from the user specified file,
     * creaton of MST, printing the output and validating the result.
     *
     * @param args array of runtime arguments.
     * @throws Exception	if File handling operation illegal.
     */
    public static void main(final String args[]) throws Exception {
        launchHabaneroApp(new HjSuspendable() {
            public void run() {
                mst ms = new mst();
                ms.nodes = 8;
                ms.nodeSet = new MSTNode[ms.nodes];
                ms.nval = new long[ms.nodes];

                ms.initialize();
                outer:
                while (true) {
                    ms.flag = new boolean[ms.nodes];
                    ms.mstCreate(ms.weight_matrix);
                    int frag = ms.nodeSet[0].fragmentRoot;
                    for (int i = 1; i < ms.nodes; i++) {
                        if (frag != ms.nodeSet[i].fragmentRoot) {
                            continue outer;
                        }
                    }
                    break;
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
                    final int f_i = i;
                    async(new HjRunnable() {
                        public void run() {
                            nodeSet[f_i] = new MSTNode();
                            nodeSet[f_i].parent = Infinity;
                            nodeSet[f_i].fragmentRoot = f_i;
                            nodeSet[f_i].startSignal = START;
                            nodeSet[f_i].changeChild = -1;
                        }
                    });
                }
            }
        });
    }

    /**
     * Generates a minimum spanning tree.
     *
     * @param	weightMatrix	Edge Weight array.
     */
    void mstCreate(final int weightMatrix[][]) {
        finish(new HjSuspendable() {
            public void run() {
                for (int i = 0; i < nodes; i++) {
                    final int f_i = i;
                    async(new HjRunnable() {
                        public void run() {
                            if (f_i == nodeSet[f_i].fragmentRoot) {
                                nodeSet[f_i].startSignal = START;
                                findNode(f_i, weightMatrix);
                                flag[f_i] = true;
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
                            if (flag[f_i]) {
                                invertPath(f_i, nodeSet[f_i].pair.u);
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
                            if (nodeSet[f_i].fragmentRoot == Infinity) {
                                nodeSet[f_i].fragmentRoot = f_i;
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
                            if (f_i == nodeSet[f_i].fragmentRoot) {
                                sendJoinSignal(f_i, nodeSet[f_i].pair.v);
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
                            flag[f_i] = false;
                            if (nodeSet[f_i].fragmentRoot == f_i && nodeSet[f_i].pair.u == f_i && getFragmentRoot(nodeSet[f_i].pair.v) != f_i) {
                                flag[f_i] = true;
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
                            if (flag[f_i]) {
                                fragmentAdd(f_i);
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
                            if (nodeSet[f_i].fragmentRoot == f_i) {
                                transmit(f_i, nodeSet[f_i].fragmentRoot);
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
                            nodeSet[f_i].startSignal = -1;
                            nodeSet[f_i].joinSignal.clear();
                            nodeSet[f_i].changeChild = -1;
                            nodeSet[f_i].changeSignal = -1;
                        }
                    });
                }
            }
        });
    }

    /**
     * Performs the task of finding blue edge for the fragment.
     *
     * @param froots	Root of a fragment.
     * @param	weightMatrix	Edge Weight array.
     */
    void findNode(int aNode, final int weightMatrix[][]) {
        final int f_aNode = aNode;
        final boolean[] flags = {false};
        if (nodeSet[aNode].startSignal == START) {
            nodeSet[aNode].pair.u = aNode;
            nodeSet[aNode].pair.v = aNode;
            if (nodeSet[aNode].children.size() == 0) {
                findMinimum(aNode, weightMatrix);
                flags[0] = true;
            }

            if (!flags[0]) {
                finish(new HjSuspendable() {
                    public void run() {
                        for (int i = 0; i < nodeSet[f_aNode].children.size(); i++) {
                            final int f_i = i;
                            async(new HjRunnable() {
                                public void run() {
                                    int child = nodeSet[f_aNode].children.get(f_i);
                                    setSignal(child, START);
                                    findNode(child, weightMatrix);
                                    setSignal(child, -1);
                                    final blueEdge cpair = getPair(child);

                                    isolated(new HjRunnable() {
                                        public void run() {
                                            if (weightMatrix[nodeSet[f_aNode].pair.u][nodeSet[f_aNode].pair.v] > weightMatrix[cpair.u][cpair.v]) {
                                                nodeSet[f_aNode].pair.setPair(cpair.u, cpair.v);
                                            }
                                        }
                                    });
                                }
                            });
                        }
                    }
                });

                for (int i = 0; i < nodes; i++) {
                    if (getFragmentRoot(i) != nodeSet[aNode].fragmentRoot && adj_graph[aNode][i] == 1) {
                        if (weightMatrix[nodeSet[aNode].pair.u][nodeSet[aNode].pair.v] > weightMatrix[aNode][i]) {
                            nodeSet[aNode].pair.setPair(aNode, i);
                        }
                    }
                }
            }
        }
    }

    /**
     * Finds the minimum weighted edge having <code>aNode<\code> as one of the
     * vertices.
     *
     * @param aNode	Node whose minimum weighted edge has to be determined.
     * @param	weightMatrix	Edge Weight array.
     */
    void findMinimum(int aNode, int weightMatrix[][]) {
        int min = Infinity;
        for (int i = 0; i < nodes; i++) {
            if (adj_graph[aNode][i] == 1 && getFragmentRoot(i) != nodeSet[aNode].fragmentRoot) {
                if (min > weightMatrix[aNode][i]) {
                    min = weightMatrix[aNode][i];
                    nodeSet[aNode].pair.setPair(aNode, i);
                }
            }
        }
    }

    /**
     * Performs the task of merging two fragments.
     *
     * @param aNode	Node whose fragment will merge with another fragment.
     */
    void fragmentAdd(int aNode) {
        boolean flag = false;
        if (nodeSet[aNode].joinSignal.size() > 0) {
            Merge mgr = new Merge();
            for (int i = 0; i < nodeSet[aNode].joinSignal.size(); i++) {
                mgr = nodeSet[aNode].joinSignal.get(i);
                if (mgr.v == nodeSet[aNode].pair.v) {
                    flag = true;
                    break;
                }
            }
            if (flag) {
                if (aNode < nodeSet[aNode].pair.v) {
                    nodeSet[aNode].fragmentRoot = aNode;
                    addChild(aNode, nodeSet[aNode].pair.v);
                    setParent(nodeSet[aNode].pair.v, aNode);
                    setFragmentRoot(nodeSet[aNode].pair.v, aNode);
                }
            }
        }
        if (!flag) {
            addChild(nodeSet[aNode].pair.v, aNode);
            nodeSet[aNode].parent = nodeSet[aNode].pair.v;
            nodeSet[aNode].fragmentRoot = Infinity;
        }
    }

    /**
     * Transmits the root of a fragment to all the other nodes of the fragment.
     */
    void transmit(int aNode, int uNode) {
        final int f_aNode = aNode;
        finish(new HjSuspendable() {
            public void run() {
                for (int i = 0; i < nodeSet[f_aNode].children.size(); i++) {
                    final int f_i = i;
                    async(new HjRunnable() {
                        public void run() {
                            setFragmentRoot(nodeSet[f_aNode].children.get(f_i), f_aNode);
                            transmit(nodeSet[f_aNode].children.get(f_i), f_aNode);
                        }
                    });
                }
            }
        });
    }

    /**
     * Inverts the path, that is if the root and source of the pair (u) are
     * different then the parent-child relationships on path from u to root are
     * reversed.
     *
     * @param aNode	node on path from root to <code>uNode<\code>
     * @param uNode	source of the pair (u)
     */
    void invertPath(int aNode, int uNode) {
        final int f_aNode = aNode;
        final int f_uNode = uNode;
        if (aNode == uNode) {
            nodeSet[aNode].fragmentRoot = Infinity;
            if (nodeSet[aNode].parent != Infinity) {
                nodeSet[aNode].children.add(nodeSet[aNode].parent);
                setSignal(nodeSet[aNode].parent, CHANGE);
                childToRemove(aNode, nodeSet[aNode].parent);
                nodeSet[aNode].parent = Infinity;
            }
        } else {
            finish(new HjSuspendable() {
                public void run() {
                    for (int i = 0; i < nodeSet[f_aNode].children.size(); i++) {
                        final int f_i = i;
                        async(new HjRunnable() {
                            public void run() {
                                invertPath(nodeSet[f_aNode].children.get(f_i), f_uNode);
                            }
                        });
                    }
                }
            });

            if (nodeSet[aNode].changeSignal == CHANGE) {
                nodeSet[aNode].fragmentRoot = uNode;
                for (int j = nodeSet[aNode].children.size() - 1; j >= 0; j--) {
                    int child = nodeSet[aNode].children.get(j);
                    if (child == nodeSet[aNode].changeChild) {
                        nodeSet[aNode].children.remove(j);
                    }
                }

                if (nodeSet[aNode].parent != Infinity) {
                    setSignal(nodeSet[aNode].parent, CHANGE);
                    nodeSet[aNode].children.add(nodeSet[aNode].parent);
                    childToRemove(aNode, nodeSet[aNode].parent);
                    nodeSet[aNode].parent = nodeSet[aNode].changeChild;
                    nodeSet[aNode].changeChild = -1;
                } else {
                    nodeSet[aNode].parent = nodeSet[aNode].changeChild;
                    nodeSet[aNode].changeChild = -1;
                }
            }
        }
    }

    /**
     * Specifies the child to remove from a node.
     *
     * @param child	node which has to be unmarked as a child.
     * @param parent	node whose child has to be removed.
     */
    void childToRemove(int child, int parent) {
        nodeSet[parent].changeChild = child;
    }

    /**
     * Sets the signal of a node as <code>START<\code> or <code>CHANGE<\code>.
     *
     * @param aNode	node for which signal has to be set
     * @param uSignal	Value of signal.
     */
    void setSignal(int aNode, int uSignal) {
        if (uSignal == CHANGE) {
            nodeSet[aNode].changeSignal = CHANGE;
        } else if (uSignal == START) {
            nodeSet[aNode].startSignal = START;
        } else {
            nodeSet[aNode].startSignal = -1;
        }
    }

    /**
     * Adds a child to the set of children of a node.
     *
     * @param aNode node which will get a new child.
     * @param child	node which is assigned a new parent node.
     */
    void addChild(int aNode, int child) {
        final int f_aNode = aNode;
        final int f_child = child;
        isolated(new HjRunnable() {
            public void run() {
                nodeSet[f_aNode].children.add(f_child);
            }
        });
    }

    /**
     * Provides the destination node (v) of the blue edge pair.
     *
     * @param aNode	node whose blue edge pair information needed
     * @return	destination node (v) of blue edge pair
     */
    int getPairV(int aNode) {
        int retVal = nodeSet[aNode].pair.v;
        return retVal;
    }

    /**
     * Provides the blue edge pair.
     *
     * @param aNode	node whose blue edge pair needed
     * @return	blue edge pair
     */
    blueEdge getPair(int aNode) {
        blueEdge bedge = new blueEdge();
        bedge.u = nodeSet[aNode].pair.u;
        bedge.v = nodeSet[aNode].pair.v;
        return bedge;
    }

    /**
     * Sets the parent of <code>aNode<\code>.
     *
     * @param aNode	node whose parent needs to be changed.
     * @param Parent	node which will act as parent
     */
    void setParent(int aNode, int Parent) {
        nodeSet[aNode].parent = Parent;
    }

    /**
     * Sets the fragment root of <code>aNode<\code>.
     *
     * @param aNode	node whose fragment root needs to be updated
     * @param root	node which will act as fragment rooot
     */
    void setFragmentRoot(int aNode, int root) {
        nodeSet[aNode].fragmentRoot = root;
    }

    /**
     * Provides the fragment root of <code>aNode<\code>.
     *
     * @param aNode	node whose fragment root needed
     * @return	fragment root
     */
    int getFragmentRoot(int aNode) {
        return nodeSet[aNode].fragmentRoot;
    }

    /**
     * Sends the join signal to a specified node.
     *
     * @param sender	node which sends the <code>JOIN<\code> signal
     * @param receiver	node which will receive the signal.will act as fragment
     * rooot
     */
    void sendJoinSignal(int sender, int receiver) {
        final int f_sender = sender;
        final int f_receiver = receiver;
        isolated(new HjRunnable() {
            public void run() {
                Merge mgr = new Merge();
                mgr.setData(JOIN, f_sender);
                nodeSet[f_receiver].joinSignal.add(mgr);
            }
        });
    }
}

/**
 * Specifies the structure for sending a merge request.
 */
class Merge {

    int join;
    int v;

    void setData(int join, int v) {
        this.join = join;
        this.v = v;
    }
}

/**
 * Specifies the structure for a blue edge pair.
 */
class blueEdge {

    int u;
    int v;

    void setPair(int u, int v) {
        this.u = u;
        this.v = v;
    }
}

/**
 * <code>node</code> specifies the structure for each abstract node, part of the
 * MST algorithm.
 */
class MSTNode {

    /**
     * Specifies the fragment root for a node.
     */
    int fragmentRoot;

    /**
     * States the parent of a node.
     */
    int parent;

    int startSignal;
    int changeSignal, changeChild;

    /**
     * Stores a blue edge pair.
     */
    blueEdge pair = new blueEdge();

    /**
     * Stores the received join signals.
     */
    ArrayList<Merge> joinSignal = new ArrayList<>();

    /**
     * Identifies the children of a node.
     */
    ArrayList<Integer> children = new ArrayList<>();
}
