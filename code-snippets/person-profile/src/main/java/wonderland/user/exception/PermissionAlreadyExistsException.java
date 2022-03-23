package wonderland.user.exception;

public class PermissionAlreadyExistsException extends RuntimeException {
    public PermissionAlreadyExistsException(String name, String application) {
        super("Permission  " + name + ":" + application + " already exists");
    }
}
