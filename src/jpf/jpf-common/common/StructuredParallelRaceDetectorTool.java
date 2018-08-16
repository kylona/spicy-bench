package common;

import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.ThreadInfo;

public interface StructuredParallelRaceDetectorTool {
  // For rewinding
  void resetState(Object state);
  Object getImmutableState();
  // Event handling
  void handleRead(ThreadInfo ti, ElementInfo ei);
  void handleWrite(ThreadInfo ti, ElementInfo ei);
  void handleAcquire(ThreadInfo ti);
  void handleRelease(ThreadInfo ti);
  void handleFork(ThreadInfo parent, ThreadInfo child);
  void handleJoin(ThreadInfo parent, ThreadInfo child);
  // Properties
  boolean race();
  String error();
}
