package wonderland.user.exception;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String phoneNumber, String email) {
        super("User with phoneNumber " + phoneNumber + " or email " + email + " already exists");
    }
}
