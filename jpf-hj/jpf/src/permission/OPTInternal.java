package permission;

import gov.nasa.jpf.vm.ThreadInfo;
import static permission.OPTSupport.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

enum Null implements PermissionTracker {

    ONLY; /* singleton */


    public PermissionTracker acquireR(ThreadInfo ti) {
        return new PrivateRead(ti.getId(), 1);
    }

    public PermissionTracker releaseR(ThreadInfo ti) {
        return violation("no read permission");
    }

    public PermissionTracker acquireW(ThreadInfo ti) {
        return new PrivateWrite(ti.getId(), 0, 1);
    }

    public PermissionTracker releaseW(ThreadInfo ti) {
        return violation("no write permission");
    }

    public PermissionTracker copy() {
        return Null.ONLY;
    }
}

class PrivateRead implements PermissionTracker {

    private final long owner;
    private final int readerCount;

    PrivateRead(long ownerId, int count) {
        owner = ownerId;
        readerCount = count;
        assert (readerCount > 0);
    }

    public PermissionTracker acquireR(ThreadInfo ti) {
        return owner == ti.getId()
                ? new PrivateRead(owner, readerCount + 1)
                : new SharedRead(owner, readerCount, ti.getId());
    }

    public PermissionTracker releaseR(ThreadInfo ti) {
        if (owner != ti.getId()) {
            System.out.println(ti.getId());
            violation("no read permission");
        }
        return readerCount > 1
                ? new PrivateRead(owner, readerCount - 1)
                : Null.ONLY;
    }

    public PermissionTracker acquireW(ThreadInfo ti) {
        if (owner != ti.getId()) {
            violation("concurrent reader/writer");
        }
        return new PrivateWrite(owner, readerCount, 1);
    }

    public PermissionTracker releaseW(ThreadInfo ti) {
        return violation("no write permission");
    }

    public PermissionTracker copy() {
        return new PrivateRead(owner, readerCount);
    }
}

class SharedRead implements PermissionTracker {

    private final Map<Long, Integer> owners;

    /**
     * Constructor for PrivateRead to SharedRead Takes the current owner and its
     * count, plus the new owner (assumes new owner will have a count of 1)
     */
    SharedRead(long ownerA, int countA, long ownerB) {
        HashMap<Long, Integer> tmp = new HashMap<>();
        tmp.put(ownerA, countA);
        tmp.put(ownerB, 1);
        owners = Collections.unmodifiableMap(tmp);
        assert (countA >= 1);
        assert (owners.size() >= 2);
    }

    /**
     * Constructor for SharedRead acquireR Takes the old (immutable) owners map,
     * makes a copy, and adds 1 to incOwner's reader count (or sets to 1 if
     * previously unset)
     */
    SharedRead(Map<Long, Integer> oldOwners, long incOwner) {
        HashMap<Long, Integer> tmp = new HashMap<>(oldOwners);
        Integer count = tmp.get(incOwner);
        tmp.put(incOwner, (count == null ? 1 : count + 1));
        owners = Collections.unmodifiableMap(tmp);
        assert (owners.size() >= 2);
    }

    /**
     * Constructor for SharedRead releaseR Takes the old (immutable) owners map,
     * makes a copy, and subtracts 1 to decOwner's reader count (or removes it
     * from the map if the count becomes 0)
     */
    SharedRead(long decOwner, Map<Long, Integer> oldOwners) {
        HashMap<Long, Integer> tmp = new HashMap<>(oldOwners);
        Integer count = tmp.get(decOwner);
        if (--count == 0) {
            tmp.remove(decOwner);
        } else {
            tmp.put(decOwner, count);
        }
        owners = Collections.unmodifiableMap(tmp);
        assert (owners.size() >= 2);
    }

    /**
     * Constructor for copying
     */
    private SharedRead(Map<Long, Integer> owners) {
        this.owners = new HashMap(owners);
    }

    public PermissionTracker acquireR(ThreadInfo ti) {
        return new SharedRead(owners, ti.getId());
    }

    public PermissionTracker releaseR(ThreadInfo ti) {
        long tid = ti.getId();
        Integer count = owners.get(tid);
        // case: threadId is not in owners map
        if (count == null) {
            return violation("no read permission");
        } // case: only one owner remains in owners map
        else if (--count == 0 && owners.size() == 2) {
            Map.Entry<Long, Integer> entry;
            Iterator<Map.Entry<Long, Integer>> it = owners.entrySet().iterator();
            entry = it.next();
            if (entry.getKey() == tid) {
                entry = it.next();
            }
            return new PrivateRead(entry.getKey(), entry.getValue());
        } // case: default (still has multiple readers)
        else {
            return new SharedRead(tid, owners);
        }
    }

    public PermissionTracker acquireW(ThreadInfo ti) {
        return violation("concurrent reader/writer");
    }

    public PermissionTracker releaseW(ThreadInfo ti) {
        return violation("no write permission");
    }

    public PermissionTracker copy() {
        return new SharedRead(owners);
    }
}

/**
 * Write permission implies read permission
 */
class PrivateWrite implements PermissionTracker {

    private final long owner;
    private final int readerCount;
    private final int writerCount;

    PrivateWrite(long ownerId, int rCount, int wCount) {
        owner = ownerId;
        readerCount = rCount;
        writerCount = wCount;
        assert (rCount >= 0);
        assert (wCount >= 1);
    }

    public PermissionTracker acquireR(ThreadInfo ti) {
        return owner == ti.getId()
                ? new PrivateWrite(owner, readerCount + 1, writerCount)
                : violation("concurrent reader/writer");
    }

    public PermissionTracker releaseR(ThreadInfo ti) {
        return (owner == ti.getId() && readerCount > 0)
                ? new PrivateWrite(owner, readerCount - 1, writerCount)
                : violation("no read permission");
    }

    public PermissionTracker acquireW(ThreadInfo ti) {
        return owner == ti.getId()
                ? new PrivateWrite(owner, readerCount, writerCount + 1)
                : violation("concurrent writer - owner: "
                        + owner + " acquirer: "
                        + ti.getId());
    }

    public PermissionTracker releaseW(ThreadInfo ti) {
        // case: wrong thread
        if (owner != ti.getId()) {
            return violation("no write permission");
        } // case: last writer
        else if (writerCount == 1) {
            return readerCount == 0
                    ? Null.ONLY
                    : new PrivateRead(owner, readerCount);
        } // case: default (not last writer)
        else {
            return new PrivateWrite(owner, readerCount, writerCount - 1);
        }
    }

    public PermissionTracker copy() {
        return new PrivateWrite(owner, readerCount, writerCount);
    }
}
