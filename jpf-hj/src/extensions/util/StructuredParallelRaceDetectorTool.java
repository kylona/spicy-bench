package extensions.util;

import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.Instruction;

public interface StructuredParallelRaceDetectorTool {
  // For rewinding
  void resetState(Object state);
  Object getImmutableState();
  // Event handling
  void handleAccess(int tid, Instruction insn);
  void handleAcquire(int tid);
  void handleRelease(int tid);
  void handleFork(int parent, int child);
  void handleJoin(int parent, int child);
  // Propertides
  boolean race();
  String error();
}
