package cg.omp.trace;

public class Read extends ReadWrite {
  public Read(String name, int threadId, int address) {
    super(name, threadId, address, true);
  }
}
