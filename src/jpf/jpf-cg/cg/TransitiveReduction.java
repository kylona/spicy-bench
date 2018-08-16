package cg;

import java.util.HashSet;
import java.util.Set;

import org.jgrapht.graph.SimpleDirectedGraph;

public class TransitiveReduction {

    private TransitiveReduction() {
    }

    public static <V, E> void closeSimpleDirectedGraph(SimpleDirectedGraph<V, E> graph) {
        Set<V> vertexSet = graph.vertexSet();
        Set<E> removeEdges = new HashSet<E>();

        for (V v1 : vertexSet) {
            for (E v1OutEdge : graph.outgoingEdgesOf(v1)) {
                V v2 = graph.getEdgeTarget(v1OutEdge);
                removeEdges.addAll(edgesToRemove(graph, v1, v2));
            }
        }
        for (E edge : removeEdges) {
            graph.removeEdge(edge);
        }
    }

    private static <E, V> Set<E> edgesToRemove(SimpleDirectedGraph<V, E> graph, V parent, V child) {
        Set<E> edges = new HashSet<E>();
        for (E childOutEdge : graph.outgoingEdgesOf(child)) {
            V v2 = graph.getEdgeTarget(childOutEdge);
            E parentTov2 = graph.getEdge(parent, v2);
            if (parentTov2 != null) {
                edges.add(parentTov2);
            }
            edges.addAll(edgesToRemove(graph, parent, v2));
        }
        return edges;
    }
}
