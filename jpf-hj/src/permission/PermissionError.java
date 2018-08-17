package permission;

public class PermissionError extends RuntimeException {

    public PermissionError(String msg) {
        super(msg);
    }
}
