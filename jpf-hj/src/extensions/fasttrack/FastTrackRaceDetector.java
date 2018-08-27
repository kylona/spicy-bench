package extensions.fasttrack;

import extensions.util.StructuredParallelRaceDetector;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.Config;

public class FastTrackRaceDetector extends StructuredParallelRaceDetector {
  public FastTrackRaceDetector(Config conf, JPF jpf) {
    super(conf, jpf, new FastTrackTool());
  }
}
