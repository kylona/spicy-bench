package cg;

public class IsolateNodeTangle {
  private Node first;
  private Node second;

  public IsolateNodeTangle(Node first, Node second) {
    this.first = first;
    this.second = second;
  }

  public boolean equals(Object o) {
    if (o == null || o.getClass() != this.getClass()) return false;
    IsolateNodeTangle input = (IsolateNodeTangle) o;

    //order does not matter
    if (input.first.equals(this.first)) {
      return input.second.equals(this.second);
    }
    if (input.first.equals(this.second)) {
      return input.second.equals(this.first);
    }
    return false;
  }
}
