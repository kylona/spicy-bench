package extensions.spbags;

import java.util.Map;
import java.util.HashMap;

/**
 * Simple implementation of the union find data structure
 */
public class UnionFind {
  Map<Integer, Integer> parent = new HashMap<>();

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
