package cg.omp.trace;

public class Write extends ReadWrite {
  public Write(String name, int threadId, int address) {
    super(name, threadId, address, false);
  }
}
