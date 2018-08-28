package extensions.util;

public interface StructuredParallelRaceDetectorTool {
  // For rewinding
  void resetState(Object state);
  Object getImmutableState();
  // Event handling
  void handleRead(int tid, int objRef, int index);
  void handleWrite(int tid, int objRef, int index);
  void handleAcquire(int tid);
  void handleRelease(int tid);
  void handleFork(int parent, int child);
  void handleJoin(int parent, int child);
  // Propertides
  boolean race();
  String error();
}
