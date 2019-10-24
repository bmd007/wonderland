package wonderland.security.authentication.exception;

public class PermissionNotFoundException extends RuntimeException {
    public PermissionNotFoundException(String name, String application) {
        super("Permission  " + name + ":" + application + " not found");
    }
}
