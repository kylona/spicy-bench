package extensions.compgraph;

import extensions.util.StructuredParallelRaceDetector;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

public class CompGraphRaceDetector extends StructuredParallelRaceDetector {
  int count = 0;
  boolean race = false;
  int maxTasks = 0;
  CompGraphChecker checker = new HappensBeforeCheck();

  public CompGraphRaceDetector(Config conf, JPF jpf) {
    super(conf, jpf, new CompGraphTool());
  }

  @Override
  public void objectReleased(VM vm, ThreadInfo ti, ElementInfo ei) {
    if (ei.toString().startsWith("hj.runtime.wsh.SuspendableActivity")) {
      CompGraphTool t = (CompGraphTool)tool;
      System.out.println("Writing graph " + (++count));
      t.writeGraph("./build/graphs/" + vm.getSUTName() + "-" + count + ".dot");
      if (t.getTaskCount() > maxTasks)
        maxTasks = t.getTaskCount();
      CompGraph graph = t.getGraph();
      race = checker.check(graph);
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
