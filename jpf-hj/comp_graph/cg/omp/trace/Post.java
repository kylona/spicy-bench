package cg.omp.trace;

public class Post extends ThreadEvent {
  private int parentThreadId;

  public Post(String name, int threadId, int parentThreadId) {
    super(name, threadId);
    this.parentThreadId = parentThreadId;
  }

  public int getThreadId() {
    return parentThreadId;
  }

  public void setThreadId(int parentThreadId) {
    this.parentThreadId = parentThreadId;
  }
}
