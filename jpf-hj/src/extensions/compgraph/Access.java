package extensions.compgraph;

public class Access {
  public final String label;
  public final boolean write;

  Access(String label, boolean write) {
    this.label = label;
    this.write = write;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null)
      return false;
    if (!(o instanceof Access))
      return false;
    Access a = (Access)o;
    if (!write && !a.write)
      return false;
    if (!label.equals(a.label))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    return label.hashCode() + (write ? 1 : 0);
  }
}
