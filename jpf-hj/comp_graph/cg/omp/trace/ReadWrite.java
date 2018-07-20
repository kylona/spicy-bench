package cg.omp.trace;

public abstract class ReadWrite extends ThreadEvent {
  private int address;
  private boolean isRead;

  public ReadWrite(String name, int threadId, int address, boolean isRead) {
    super(name, threadId);
    this.address = address;
    this.isRead = isRead;
  }

  public int getThreadId() {
    return address;
  }

  public void setThreadId(int address) {
    this.address = address;
  }

  public int getAddress() {
    return address;
  }

  public void setAddress(int address) {
    this.address = address;
  }

  public boolean isRead() {
    return isRead;
  }

  public void setRead(boolean read) {
    isRead = read;
  }

  public boolean isWrite() {
    return !isRead;
  }

  public void setWrite(boolean write) {
    isRead = !write;
  }
}
