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
  CompGraphChecker checker = new HappensBeforeCheck();

  public CompGraphRaceDetector(Config conf, JPF jpf) {
    super(conf, jpf, new CompGraphTool());
  }

  @Override
  public void objectReleased(VM vm, ThreadInfo ti, ElementInfo ei) {
    if (ei.toString().startsWith("hj.runtime.wsh.SuspendableActivity")) {
      System.out.println("Writing graph " + (++count));
      ((CompGraphTool)tool).writeGraph("./build/graphs/" + vm.getSUTName() + "-" + count + ".dot");
      CompGraph graph = ((CompGraphTool)tool).getGraph();
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

}
