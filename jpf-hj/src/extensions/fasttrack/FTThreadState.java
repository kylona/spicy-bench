package extensions.fasttrack;

import extensions.fasttrack.util.VectorClock;

public class FTThreadState extends VectorClock {
  public FTThreadState(VectorClock other) {
    super(other);
  }
  public FTThreadState(int size) {
    super(size);
  }
}
