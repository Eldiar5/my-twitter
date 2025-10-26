package twitter.exceptions;

public class TwitterIllegalArgumentException extends RuntimeException {

  public TwitterIllegalArgumentException(String message, Throwable cause) {
    super(message, cause);
  }

  public TwitterIllegalArgumentException(String message) {
        super(message);
    }
}
