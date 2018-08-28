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

  Map<Integer, ShadowSpace> fields = new HashMap<>();
  Map<String, ShadowSpace> arrays = new HashMap<>();
  Map<Integer, ShadowSpace> isolatedFields = new HashMap<>();
  Map<String, ShadowSpace> isolatedArrays = new HashMap<>();
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
    final Map<Integer, ShadowSpace> fields;
    final Map<String, ShadowSpace> arrays;
    final Map<Integer, ShadowSpace> isolatedFields;
    final Map<String, ShadowSpace> isolatedArrays;
    final UnionFind bags;
    SPBagsToolState(Map<Integer, ShadowSpace> fields,
        Map<String, ShadowSpace> arrays,
        Map<Integer, ShadowSpace> isolatedFields,
        Map<String, ShadowSpace> isolatedArrays,
        UnionFind bags) {
      //TODO make copies
      this.fields = fields;
      this.arrays = arrays;
      this.isolatedFields = isolatedFields;
      this.isolatedArrays = isolatedArrays;
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
    fields = toolState.fields;
    arrays = toolState.arrays;
    isolatedFields = toolState.isolatedFields;
    isolatedArrays = toolState.isolatedArrays;
    bags = toolState.bags;
  }

  @Override
  public Object getImmutableState() {
    return new SPBagsToolState(fields, arrays, isolatedFields, isolatedArrays, bags);
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
