package extensions.compgraph;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

public class HappensBeforeCheck implements CompGraphChecker {
  public boolean check(CompGraph graph) {
    List<CompGraphNode> nodes = new ArrayList<>();
    Iterator<CompGraphNode> it = graph.iterator();
    while (it.hasNext())
      nodes.add(it.next());
    for (int i = 0; i < nodes.size(); i++)
      for (int j = i + 1; j < nodes.size(); j++) {
        if (nodes.get(i).isReadWrite()
            && nodes.get(j).isReadWrite()
            && !(nodes.get(j).isIsolated() && nodes.get(i).isIsolated())
            && !graph.happensBefore(nodes.get(i), nodes.get(j))
            && !graph.happensBefore(nodes.get(j), nodes.get(i))
            && nodes.get(i).intersection(nodes.get(j)).size() > 0
            )
          return true;
      }
    return false;
  }
}
