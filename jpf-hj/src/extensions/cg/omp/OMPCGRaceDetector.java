package cg.omp;

import cg.omp.trace.Trace;

public class OMPCGRaceDetector {
  public static void main(String args[]) {
    if(args.length < 1) {
      System.err.println("Please specify a trace log file path");
      System.exit(1);
    }
    Trace trace = new Trace(args[0]);
  }
}
