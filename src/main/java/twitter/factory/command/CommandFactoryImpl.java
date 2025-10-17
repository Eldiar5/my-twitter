package twitter.factory.command;

import twitter.controller.v1.PostCommandController;
import twitter.controller.v1.UserCommandController;
import twitter.exceptions.CommandNotFoundException;
import twitter.factory.CommandFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

//@Component
public class CommandFactoryImpl implements CommandFactory {

    private final Map<TwitterCommandEnum, CommandHandler> factory;

    private final UserCommandController userCommandController;
    private final PostCommandController postCommandController;

//    @Injection
    public CommandFactoryImpl(
            UserCommandController userCommandController,
            PostCommandController postCommandController
    ) {
        this.factory = new HashMap<>();
        this.userCommandController = userCommandController;
        this.postCommandController = postCommandController;
        init();
    }

    public void init() {
        this.factory.put(TwitterCommandEnum.EXIT, () -> {
             try {
                 userCommandController.executeExit();
             } catch (IOException e) {
                 System.out.println("Ошибка при выполнении команды EXIT " + e.getMessage());
             }
        });
        this.factory.put(TwitterCommandEnum.HELP, () -> {
             try {
                 userCommandController.executeHelp();
             } catch (IOException e) {
                 System.out.println("Ошибка при выполнении команды HELP " + e.getMessage());
             }
        });
        this.factory.put(TwitterCommandEnum.REGISTER, () -> {
            try {
                userCommandController.executeRegister();
            } catch (IOException e) {
                System.out.println("Ошибка при выполнении команды REGISTER " + e.getMessage());
            }
        });
        this.factory.put(TwitterCommandEnum.LOGIN, () -> {
            try {
                userCommandController.executeLogin();
            } catch (IOException e) {
                System.out.println("Ошибка при выполнении команды LOGIN " + e.getMessage());
            }
        });
        this.factory.put(TwitterCommandEnum.LOGOUT, () -> {
            try {
                userCommandController.executeLogout();
            } catch (IOException e) {
                System.out.println("Ошибка при выполнении команды LOGOUT " + e.getMessage());
            }
        });
        this.factory.put(TwitterCommandEnum.INFO_BY_LOGIN, () -> {
            try {
                userCommandController.executeInfoByLogin();
            } catch (IOException e) {
                System.out.println("Ошибка при выполнении команды INFO_BY_LOGIN " + e.getMessage());
            }
        });
        this.factory.put(TwitterCommandEnum.INFO_BY_USER_TYPE, () -> {
            try {
                userCommandController.executeInfoByUserType();
            } catch (IOException e) {
                System.out.println("Ошибка при выполнении команды INFO_BY_USER_TYPE " + e.getMessage());
            }
        });
        this.factory.put(TwitterCommandEnum.INFO_ALL, () -> {
            try {
                userCommandController.executeInfoAll();
            } catch (IOException e) {
                System.out.println("Ошибка при выполнении команды INFO_ALL " + e.getMessage());
            }
        });
        this.factory.put(TwitterCommandEnum.INFO, () -> {
             try {
                 userCommandController.executeInfo();
             } catch (IOException e) {
                 System.out.println("Ошибка при выполнении команды INFO " + e.getMessage());
             }
        });


        this.factory.put(TwitterCommandEnum.ADD_POST, () -> {
            try {
                postCommandController.executeAddPost();
            } catch (IOException e) {
                System.out.println("Ошибка при выполнении команды ADD_POST " + e.getMessage());
            }
        });
        this.factory.put(TwitterCommandEnum.MY_POSTS, () -> {
            try {
                postCommandController.executeMyPosts();
            } catch (IOException e) {
                System.out.println("Ошибка при выполнении команды MY_POSTS " + e.getMessage());
            }
        });
        this.factory.put(TwitterCommandEnum.ALL_POSTS, () -> {
            try {
                postCommandController.executeAllPosts();
            } catch (IOException e) {
                System.out.println("Ошибка при выполнении команды ALL_POSTS " + e.getMessage());
            }
        });
        this.factory.put(TwitterCommandEnum.POSTS_BY_TAG, () -> {
            try {
                postCommandController.executePostsByTag();
            } catch (IOException e) {
                System.out.println("Ошибка при выполнении команды POSTS_BY_TAG " + e.getMessage());
            }
        });
        this.factory.put(TwitterCommandEnum.POSTS_BY_LOGIN, () -> {
            try {
                postCommandController.executePostsByUserLogin() ;
            } catch (IOException e) {
                System.out.println("Ошибка при выполнении команды POSTS_BY_LOGIN " + e.getMessage());
            }
        });
        this.factory.put(TwitterCommandEnum.POSTS_BY_USER_TYPE, () -> {
            try {
                postCommandController.executeAllPostsByUserType();
            } catch (IOException e) {
                System.out.println("Ошибка при выполнении команды POSTS_BY_USER_TYPE " + e.getMessage());
            }
        });
        this.factory.put(TwitterCommandEnum.DELETE_POST, () -> {
            try {
                postCommandController.executeDeletePost();
            } catch (IOException e) {
                System.out.println("Ошибка при выполнении команды DELETE_POST " + e.getMessage());
            }
        });
    }

    @Override
    public CommandHandler getHandler(String command) {
        if (!factory.containsKey(TwitterCommandEnum.getCommandByCommand(command))) {
            throw new CommandNotFoundException("Команда " + command + "не найдена");
        }
        return factory.get(TwitterCommandEnum.getCommandByCommand(command));
    }

}
