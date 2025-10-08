package twitter.factory.command;

@FunctionalInterface
public interface CommandHandler {

    void handle();

    private void doSmth() {
        System.out.println("nhudhewuh");
    };

}
