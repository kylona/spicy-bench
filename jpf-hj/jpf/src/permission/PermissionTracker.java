package permission;

import gov.nasa.jpf.vm.ThreadInfo;

/**
 * Interface for immutable permission tracker state. Invoking a request returns
 * a (possibly new) updated state.
 */
public interface PermissionTracker extends Cloneable {

    public abstract PermissionTracker acquireR(ThreadInfo ti);

    public abstract PermissionTracker releaseR(ThreadInfo ti);

    public abstract PermissionTracker acquireW(ThreadInfo ti);

    public abstract PermissionTracker releaseW(ThreadInfo ti);

    public abstract PermissionTracker copy();
}
