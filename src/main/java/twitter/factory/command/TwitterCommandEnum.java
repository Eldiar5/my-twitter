package twitter.factory.command;

public enum TwitterCommandEnum {

    EXIT("exit", "выход из системы."),
    REGISTER("register", "регистрация нового пользователя."),
    LOGIN("login", "вход в систему."),
    HELP("help", "помощь по командам системы."),
    LOGOUT("logout", "Выход с аккаунта."),
    INFO("info", "Вывод информации об авторизированном пользователе."),
    INFO_BY_LOGIN("info_by_login", "Вывод информации по логину пользователя."),
    INFO_BY_USER_TYPE("info_by_user_type", "Вывод информации по типу пользователя."),
    INFO_ALL("info_all", "Вывод информации о всех пользователях системы."),
    ADD_POST("add_post", "Добавить новую публикацию."),
    MY_POSTS("my_posts", "Вывод списка своих публикаций."),
    ALL_POSTS("all_posts", "Вывод всех публикаций в системе."),
    POSTS_BY_TAG("posts_by_tag", "Вывод публикаций по тэгу."),
    POSTS_BY_LOGIN("posts_by_login", "Вывод публикаций по логину пользователя."),
    POSTS_BY_USER_TYPE("posts_by_user_type", "Вывод публикаций по типу пользователя."),
    DELETE_POST("delete_post", "Удалить свою публикацию по ID.");

    private String command;
    private String description;

    TwitterCommandEnum(String name, String description) {
        this.command = name;
        this.description = description;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public static TwitterCommandEnum getCommandByCommand(String command) {
        command = command.toLowerCase();
        for (TwitterCommandEnum commandEnum : TwitterCommandEnum.values()) {
            if (commandEnum.getCommand().equals(command)) {
                return commandEnum;
            }
        }
        return null;
    }

    public static String getAllCommands() {
        StringBuilder stringBuilder = new StringBuilder();
        for (TwitterCommandEnum twitterCommandEnum : TwitterCommandEnum.values()) {
            stringBuilder.append(twitterCommandEnum.command).append(" - ").append(twitterCommandEnum.description).append("\n");
        }
        return stringBuilder.toString();
    }

}
