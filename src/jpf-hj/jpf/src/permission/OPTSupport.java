package permission;

public final class OPTSupport {

    public static PermissionTracker[] withArrayDefault(PermissionTracker[] attribute, int len) {
        if (attribute == null) {
            PermissionTracker[] newAttribute = new PermissionTracker[len];
            for (int i = 0; i < len; i++) {
                newAttribute[i] = Null.ONLY;
            }
            return newAttribute;
        } else {
            return attribute;
        }
    }

    private OPTSupport() {
    } /* static members only */


    static PermissionTracker violation(String msg) {
        throw new PermissionError(msg);
    }

    public static PermissionTracker withDefault(PermissionTracker x) {
        return x == null ? Null.ONLY : x;
    }

}
