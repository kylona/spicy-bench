package extensions.spbags;

import extensions.util.StructuredParallelRaceDetector;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.Config;

public class SPBagsRaceDetector extends StructuredParallelRaceDetector {
  public SPBagsRaceDetector(Config conf, JPF jpf) {
    super(conf, jpf, new SPBagsTool());
  }
}
