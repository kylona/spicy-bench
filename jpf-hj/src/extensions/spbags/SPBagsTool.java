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

  Map<String, ShadowSpace> shadows = new HashMap<>();
  Map<String, ShadowSpace> isolated = new HashMap<>();
  UnionFind bags = new UnionFind();

  private static class ShadowSpace {
    int writer;
    int reader;
    ShadowSpace(int writer, int reader) {
      this.writer = writer;
      this.reader = reader;
    }
    @Override
    public String toString() {
      return "ShadowSpace with writer(" + writer + ") and reader(" + reader + ")";
    }
  }

  private static class SPBagsToolState {
    final Map<String, ShadowSpace> shadows;
    final Map<String, ShadowSpace> isolated;
    final UnionFind bags;
    SPBagsToolState(Map<String, ShadowSpace> shadows,
        Map<String, ShadowSpace> isolated,
        UnionFind bags) {
      //TODO make copies
      this.shadows = shadows;
      this.isolated = isolated;
      this.bags = bags;
    }
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      return sb.toString();
    }
  }

  @Override
  public void resetState(Object state) {
    SPBagsToolState toolState = (SPBagsToolState)state;
    shadows = toolState.shadows;
    isolated = toolState.isolated;
    bags = toolState.bags;
  }

  @Override
  public Object getImmutableState() {
    return new SPBagsToolState(shadows, isolated, bags);
  }

  @Override
  public void handleRead(int tid, String label) {
  }

  @Override
  public void handleWrite(int tid, String label) {
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
