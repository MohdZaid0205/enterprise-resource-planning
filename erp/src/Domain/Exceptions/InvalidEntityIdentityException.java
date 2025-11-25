package Domain.Exceptions;

public class InvalidEntityIdentityException extends RuntimeException {
    public InvalidEntityIdentityException(String message) {
        super(message);
    }
}
