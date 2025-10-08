package twitter.exceptions;

public class UnknownUserTypeException extends Exception {
    public UnknownUserTypeException() {}

    public UnknownUserTypeException(String message) {
        super(message);
    }
}
