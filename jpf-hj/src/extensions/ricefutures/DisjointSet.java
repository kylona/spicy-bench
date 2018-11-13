package extensions.ricefutures;

import org.jgraph.graph.DefaultEdge;

import java.util.Map;
import java.util.HashMap;

/**
 * Simple implementation of the union find data structure
 */
public class UnionFind {
    Map<Integer, Integer> parent = new HashMap<>();

    // Each of these belong with a particular disjoint set, and are a part of the futures algorithm
    private int pre, post; //orderings based on visitation of nodes
    private Set<DefaultEdge> nonTreeEdges;
    private Node parent; //parent task TODO: what type should the parent be? 
    private Node lsa; //least significant ancestor

    public void makeSet(int i) {
        parent.put(i, i);
    }

    public int find(int i) {
        if (parent.get(i) == i) {
        return i;
        }
        int result = find(parent.get(i));
        parent.put(i, result);
        return result;
    }

    public void union(int i, int j) {
        parent.put(j, i);
    }
}
