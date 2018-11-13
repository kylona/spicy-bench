import extensions.util.StructuredParallelRaceDetector;

public class FuturesTool implements StructureParallelRaceDetectorTool {

    /** For rewinding
    void resetState(Object state);
    Object getImmutableState();
    // Event handling
    void handleRead(int tid, String uniqueLabel);
    void handleWrite(int tid, String uniqueLabel);
    void handleAcquire(int tid);
    void handleRelease(int tid);
    void handleFork(int parent, int child);
    void handleJoin(int parent, int child);
    // Propertides
    boolean race();
    String error();
    */

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
      


} 