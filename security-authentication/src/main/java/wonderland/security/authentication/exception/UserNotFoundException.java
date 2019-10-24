package wonderland.security.authentication.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String phoneNumber, String email) {
        super("User with phoneNumber " + phoneNumber + " or email " + email + " not found");
    }
}
