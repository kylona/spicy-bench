package extensions.compgraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

public class CompGraphNode {
  public static enum NodeType { ASYNC, JOIN, ACTIVITY, ISOLATED }
  static int count = 0;

  NodeType type;
  Map<String, ObjectAccess> objAccesses = new HashMap<>();
  Map<String, ArrayAccess> arrAccesses = new HashMap<>();
  int index;
  boolean hasIncomingIsolationEdge = false;
  boolean hasOutgoingIsolationEdge = false;
  boolean readyForJoin = false;

  private CompGraphNode(NodeType type) {
    this.type = type;
    this.index = count++;
  }

  public static CompGraphNode mkForkNode() {
    return new CompGraphNode(NodeType.ASYNC);
  }

  public static CompGraphNode mkJoinNode() {
    return new CompGraphNode(NodeType.JOIN);
  }

  public static CompGraphNode mkActivityNode() {
    return new CompGraphNode(NodeType.ACTIVITY);
  }

  public static CompGraphNode mkIsolatedNode() {
    return new CompGraphNode(NodeType.ISOLATED);
  }

  private String arrLabel(String label) {
    return label.substring(0, label.indexOf('['));
  }

  private int arrIndex(String label) {
    int i = label.indexOf('[');
    return Integer.parseInt(label.substring(i + 1, label.length() - 1));
  }

  public void addAccess(String label, boolean write) {
    if (label.startsWith("array@")) {
      String k = arrLabel(label);
      int i = arrIndex(label);
      if (arrAccesses.containsKey(k)) {
        arrAccesses.get(k).insert(i, write);
      } else
        arrAccesses.put(k, new ArrayAccess(k, i, write));
    } else if (!objAccesses.containsKey(label) || !objAccesses.get(label).write)
      objAccesses.put(label, new ObjectAccess(label, write));
  }

  public boolean isJoin() {
    return type == NodeType.JOIN;
  }

  public boolean isReadWrite() {
    return objAccesses.size() > 0 || arrAccesses.size() > 0;
  }

  public boolean isAsync() {
    return type == NodeType.ASYNC;
  }

  public boolean isIsolated() {
    return type == NodeType.ISOLATED;
  }

  public int getIndex() {
    return index;
  }

  public void setHasOutgoingIsolationEdge(boolean b) {
    hasOutgoingIsolationEdge = b;
  }

  public void setHasIncomingIsolationEdge(boolean b) {
    hasIncomingIsolationEdge = b;
  }

  public boolean hasOutgoingIsolationEdge() {
    return hasOutgoingIsolationEdge;
  }

  public boolean hasIncomingIsolationEdge() {
    return hasIncomingIsolationEdge;
  }

  public Set<String> intersection(CompGraphNode n) {
    Set<String> intersection = new HashSet<>();
    // objects
    for (String k : objAccesses.keySet()) {
      if (n.objAccesses.containsKey(k) && objAccesses.get(k).conflicts(n.objAccesses.get(k)))
        intersection.add(k);
    }
    // arrays
    for (String k : arrAccesses.keySet()) {
      if (n.arrAccesses.containsKey(k) && arrAccesses.get(k).conflicts(n.arrAccesses.get(k)))
        intersection.add(k);
    }
    return intersection;
  }

  public void setReadyForJoin(boolean input) {
    this.readyForJoin = input;
  }

  public boolean isReadyForJoin() {
    return this.readyForJoin;
  }

  public void union(CompGraphNode n) {
    //TODO
  }

  public void clear() {
    objAccesses.clear();
    arrAccesses.clear();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(index);
    return sb.toString();
  }
}
