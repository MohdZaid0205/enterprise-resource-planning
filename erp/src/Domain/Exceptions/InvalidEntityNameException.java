package Domain.Exceptions;

public class InvalidEntityNameException extends RuntimeException {
  public InvalidEntityNameException(String message) {
    super(message);
  }
}
