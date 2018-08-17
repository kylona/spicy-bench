package cg.omp.trace;

public class ThreadEvent extends Event {
  private int threadId;

  public ThreadEvent(String name, int threadId) {
    super(name);
    this.threadId = threadId;
  }

  public int getThreadId() {
    return threadId;
  }

  public void setThreadId(int threadId) {
    this.threadId = threadId;
  }
}
