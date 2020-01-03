package wonderland.user.exception;

public class RoleAlreadyExistsException extends RuntimeException {
    public RoleAlreadyExistsException(String name) {
        super("Role with name " + name + " already exists");
    }
}
