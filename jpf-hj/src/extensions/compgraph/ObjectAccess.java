package extensions.compgraph;

/**
 * Stores access information for a shared variable.
 */
public class ObjectAccess {
  public final String label;
  public final boolean write;

  ObjectAccess(String label, boolean write) {
    this.label = label;
    this.write = write;
  }

  boolean conflicts (ObjectAccess a) {
    if (!write && !a.write)
      return false;
    if (!label.equals(a.label))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return label + (write ? " write" : " read");
  }
}
