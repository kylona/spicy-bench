package extensions.spbags;

import extensions.util.StructuredParallelRaceDetectorTool;
import gov.nasa.jpf.vm.bytecode.ReadOrWriteInstruction;

import java.util.HashMap;
import java.util.Map;

/*
 * Simple implementation of SPBags
 */
public class SPBagsTool implements StructuredParallelRaceDetectorTool {

  boolean race = false;
  String error = "";

  private static class SPBagsToolState {
  }

  @Override
  public void resetState(Object state) {
  }

  @Override
  public Object getImmutableState() {
    return null;
  }

  @Override
  public void handleRead(int tid, int objRef, int index) {
  }

  @Override
  public void handleWrite(int tid, int objRef, int index) {
  }

  @Override
  public void handleAcquire(int tid) {
  }

  @Override
  public void handleRelease(int tid) {
  }

  @Override
  public void handleFork(int parent, int child) {
  }

  @Override
  public void handleJoin(int parent, int child) {
  }

  @Override
  public boolean race() {
    return race;
  }

  @Override
  public String error() {
    return race ? error : null;
  }

  void reportRace(String err) {
    race = true;
    error = err;
  }
}
