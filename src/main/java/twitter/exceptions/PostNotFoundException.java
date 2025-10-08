package twitter.exceptions;

public class PostNotFoundException extends Exception {
    public PostNotFoundException() {}

    public PostNotFoundException(String message) {
        super(message);
    }
}
