package extensions.compgraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CompGraphNode {
  public static enum NodeType { ASYNC, JOIN, ACTIVITY, ISOLATED }
  static final int BOUND = 1;
  static int count = 0;

  NodeType type;
  Set<Access> accessSet = new HashSet<>();
  List<Access> shortAccessSet = new ArrayList<>();
  boolean largeNode = false;
  int index;
  boolean hasIncomingIsolationEdge = false;
  boolean hasOutgoingIsolationEdge = false;

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

  public void addAccess(String label, boolean write) {
    addAccess(new Access(label, write));
  }

  public void addAccess(Access a) {
    if (shortAccessSet.size() < BOUND)
      shortAccessSet.add(a);
    else if (accessSet.size() == 0) {
      accessSet.addAll(shortAccessSet);
      accessSet.add(a);
      shortAccessSet.clear();
      largeNode = true;
    }
    else
      accessSet.add(a);
  }

  public boolean isJoin() {
    return type == NodeType.JOIN;
  }

  public boolean isReadWrite() {
    return (largeNode && accessSet.size() > 0) || (!largeNode && shortAccessSet.size() > 0);
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

  public Set<Access> intersection(CompGraphNode n) {
    Set<Access> intersection = new HashSet<>();
    Collection<Access> small;
    Collection<Access> large;
    if (largeNode) {
      if (n.largeNode) {
        small = (n.accessSet.size() > accessSet.size()) ? accessSet : n.accessSet;
        large = (n.accessSet.size() > accessSet.size()) ? n.accessSet : accessSet;
      } else {
        small = n.shortAccessSet;
        large = accessSet;
      }
    } else if (n.largeNode) {
      small = shortAccessSet;
      large = n.accessSet;
    } else {
      small = n.shortAccessSet;
      large = new HashSet<>();
      for (Access a : shortAccessSet)
        large.add(a);
    }
    for (Access a : small) {
      if (large.contains(a))
        intersection.add(a);
    }
    return intersection;
  }

  public void union(CompGraphNode n) {
    Collection<Access> accesses = n.largeNode ? n.accessSet : n.shortAccessSet;
    for (Access a : accesses)
      addAccess(a);
  }

  public void clear() {
    accessSet.clear();
    shortAccessSet.clear();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(super.toString() + "\n");
    sb.append("Short Access Set\n");
    for (Access a : shortAccessSet)
      sb.append(a.label + (a.write ? " write\n" : " read\n"));
    sb.append("Access Set\n");
    for (Access a : accessSet)
      sb.append(a.label + (a.write ? " write\n" : " read\n"));
    return sb.toString();
  }
}
