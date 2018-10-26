package extensions.zipper;

import extensions.util.StructuredParallelRaceDetector;
import extensions.compgraph.CompGraphNode;
import extensions.compgraph.CompGraph;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

public class ZipperRaceDetector extends StructuredParallelRaceDetector {
  int count = 0;
  boolean race = false;
  int maxTasks = 0;
  ZipperCheck checker = new ZipperCheck();

  public ZipperRaceDetector(Config conf, JPF jpf) {
    super(conf, jpf, new ZipperTool());
  }

  @Override
  public void objectReleased(VM vm, ThreadInfo ti, ElementInfo ei) {
    ZipperTool t = (ZipperTool) tool;
    if (ei.toString().startsWith("hj.runtime.wsh.SuspendableActivity")) {
      System.out.println("Writing graph " + (++count));
      t.writeGraph("./build/graphs/" + vm.getSUTName() + "-" + count + ".dot");
      if (t.getTaskCount() > maxTasks)
        maxTasks = t.getTaskCount();
      CompGraph graph = t.getGraph();
      List<CompGraphNode> isolationOrder = t.getIsolationOrder();
      race = checker.check(graph, isolationOrder);
    }
  }

  @Override
  public String getErrorMessage () {
    return race ? "Data race detected" : null;
  }

  @Override
  public boolean check(Search search, VM vm) {
    return !race;
  }

  @Override
  public void searchFinished(Search search) {
    super.searchFinished(search);
    System.out.println("Tasks: " + maxTasks);
  }

}
