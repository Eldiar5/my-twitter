package twitter.factory;

import twitter.factory.command.CommandHandler;

public interface CommandFactory {

    CommandHandler getHandler(String command);

}
