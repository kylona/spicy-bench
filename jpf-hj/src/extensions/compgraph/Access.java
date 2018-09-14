package extensions.compgraph;

/**
 * Stores access information for a shared variable.
 * We override the equals method to return whether
 * two accesses conflict to make set intersection
 * a data race check. This is efficient for tasks
 * that write shared variables but if a task exclusively
 * reads shared variables then they all get added
 * to the access set.
 */
public class Access {
  public final String label;
  public final boolean write;

  Access(String label, boolean write) {
    this.label = label;
    this.write = write;
  }

  boolean conflicts (Access a) {
    if (!write && !a.write)
      return false;
    if (!label.equals(a.label))
      return false;
    return true;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null)
      return false;
    if (!(o instanceof Access))
      return false;
    Access a = (Access)o;
    //Return whether the access conflicts
    return conflicts(a);
  }

  @Override
  public int hashCode() {
    //Reads and writes must return same hash code
    return label.hashCode();
  }

  @Override
  public String toString() {
    return label + (write ? " write" : " read");
  }
}
