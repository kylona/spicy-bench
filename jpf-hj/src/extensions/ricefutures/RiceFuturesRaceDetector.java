package extensions.ricefutures;

import extensions.util.StructuredParallelRaceDetector;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.Config;

public class RiceFuturesRaceDetector extends StructuredParallelRaceDetector {
  public RiceFuturesRaceDetector(Config conf, JPF jpf) {
    super(conf, jpf, new FuturesTool());
  }
}
