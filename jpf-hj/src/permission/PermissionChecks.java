package permission;

/**
 * This is basically a mockup of what JPF will do with its internal object
 * attributes. This should be implemented as a JPF Model Class.
 */
public final class PermissionChecks {

    private PermissionChecks() {
    }

    //Acquire Read API
    public static <T> void acquireR(T xs) {
    }

    public static <T> void acquireR(T xs, int idx) {
    }

    public static <T> void acquireR(T xs, int start, int end) {
    }

    public static <T> void releaseR(T xs) {
    }

    public static <T> void releaseR(T xs, int idx) {
    }

    public static <T> void releaseR(T xs, int start, int end) {
    }

    public static <T> void acquireW(T xs) {
    }

    public static <T> void acquireW(T xs, int idx) {
    }

    public static <T> void acquireW(T xs, int start, int end) {
    }

    public static <T> void releaseW(T xs) {
    }

    public static <T> void releaseW(T xs, int idx) {
    }

    public static <T> void releaseW(T xs, int start, int end) {
    }
}
