package cg;

import gov.nasa.jpf.vm.*;

import gov.nasa.jpf.vm.choice.ThreadChoiceFromSet;
import gov.nasa.jpf.util.ObjectList.TypedIterator;


public class IsolateChoiceGenerator extends ThreadChoiceFromSet {

  private GraphState resetState = null;
  CGRaceDetector owner = null;
  private String dir = null;
  private VM vm = null;

  public IsolateChoiceGenerator(CGRaceDetector owner, String id, ThreadInfo[] timeoutRunnables, boolean bool, String dir, VM vm) {
    super(id, timeoutRunnables, bool);
    this.owner = owner;
    this.resetState = new GraphState(owner);
    this.dir = dir;
    this.vm = vm;
  }

  @Override
  public void advance() {
    System.out.println("Reseting Graph To Check Next Scheduling");
    resetState.resetGraphState(owner);
    super.advance();
  }

}
