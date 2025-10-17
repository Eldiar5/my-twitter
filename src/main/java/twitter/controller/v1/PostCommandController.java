package twitter.controller.v1;

import twitter.entity.post.Post;
import twitter.entity.user.User;
import twitter.entity.user.UserType;
import twitter.exceptions.PostNotFoundException;
import twitter.exceptions.UnknownUserTypeException;
import twitter.exceptions.UserNotFoundException;
import twitter.mapper.viewMapper.PostViewMapper;
import twitter.security.Security;
import twitter.service.PostService;
import twitter.service.UserService;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.InputMismatchException;
import java.util.List;

//@Component
public class PostCommandController {

    private final Security securityComponent;
    private final PostViewMapper postViewMapper;
    private final PostService postService;
    private final UserService userService;
    private final BufferedReader input;
    private final BufferedWriter output;
    private final String userIp;


//    @Injection
    public PostCommandController(
            Security securityComponent,
            PostViewMapper postViewMapper,
            PostService postService,
            UserService userService, BufferedReader input, BufferedWriter output, String userIp
    ) {
        this.securityComponent = securityComponent;
        this.postViewMapper = postViewMapper;
        this.postService = postService;
        this.userService = userService;
        this.input = input;
        this.output = output;
        this.userIp = userIp;
    }

    public void executeAddPost() throws IOException {
        if (securityComponent.getAuthenticationUser(userIp) == null) {
            output.append("Для публикации поста необходимо войти в систему").append("\n").flush();
            return;
        }

        output.append("<<<< Публикация новго поста >>>>").append("\n").flush();

        output.append("Введите тему публикации: ").flush();
        String topic = this.input.readLine();
        if (topic.trim().isEmpty()) {
            output.append("Тема публикации не может быть пустой.").append("\n").flush();
            return;
        }

        output.append("Введите текст публикации: ").flush();
        String text = this.input.readLine();
        if (text.trim().isEmpty()) {
            output.append("Текст публикации не может быть пустой.").append("\n").flush();
            return;
        }

        output.append("Добавьте теги к публикации(можно оставить пустым, для отделения тегов используйте ','): ").flush();
        String inputTags = this.input.readLine();
        inputTags = inputTags.trim();
        String[] tagsArray = (inputTags != null && !inputTags.isEmpty()) ? inputTags.split(",") : new String[0];

        User authenticatedUser = securityComponent.getAuthenticationUser(userIp);

        Post post = new Post();
        post.setAuthorId(Math.toIntExact(authenticatedUser.getId()));
        post.setTopic(topic);
        post.setText(text);
        post.setTags(tagsArray);

        try {
            Post createdPost = postService.savePost(post);

            String postAsString = postViewMapper.postToBeautifulString(createdPost, authenticatedUser.getLogin());
            output.append(postAsString).append("\n").flush();

        } catch (PostNotFoundException ex) {
            output.append(ex.getMessage()).append("\n").flush();
        } catch (Exception ex) {
            output.append("Произошла ошибка при сохранении поста: " + ex.getMessage() + "\n").flush();
            ex.printStackTrace();
        }

        output.append("<<<< Конец публикации. >>>>").append("\n").flush();

    }

    public void executeMyPosts() throws IOException {
        if (securityComponent.getAuthenticationUser(userIp) == null) {
            output.append("Для вывода списка постов необходимо войти в систему.").append("\n").flush();
            return;
        }

        output.append("<<<< Мои публикации >>>>").append("\n").flush();

        User currentUser = securityComponent.getAuthenticationUser(userIp);

        try {
            List<Post> currentPosts = postService.getPostsByUser(currentUser);

            if (currentPosts.isEmpty()) {
                throw new PostNotFoundException("У вас нет постов.");
            }

            for (Post post : currentPosts) {
                String postAsString = postViewMapper.postToBeautifulString(post, currentUser.getLogin());
                output.append(postAsString).append("\n").flush();
            }

            output.append("<<<< Конец моих публикаций >>>>").append("\n").flush();

        } catch (PostNotFoundException ex) {
            output.append(ex.getMessage()).append("\n").flush();
        } catch (Exception ex) {
            output.append("Произошла ошибка: " + ex.getMessage() + "\n").flush();
            ex.printStackTrace();
        }
    }

    public void executeAllPosts() throws IOException {

        if (securityComponent.getAuthenticationUser(userIp) == null) {
            output.append("Для вывода списка всех постов необходимо войти в систему.").append("\n").flush();
            return;
        }

        output.append("<<<< Все публикации >>>>").append("\n").flush();

        try {
            List<Post> allPosts = postService.getAllPosts();

            for (Post post : allPosts) {
                if (post != null) {
                    // Здесь нужно получить автора для каждого поста
                    User author = userService.getUserById(post.getAuthorId());
                    String postAsString = postViewMapper.postToBeautifulString(post, author.getLogin());
                    output.append(postAsString).append("\n").flush();

                }
            }

            output.append("<<<< Конец всех публикаций >>>>").append("\n").flush();

        } catch (PostNotFoundException ex) {
            output.append(ex.getMessage()).append("\n").flush();
        } catch (Exception ex) {
            output.append("Произошла ошибка: " + ex.getMessage() + "\n").flush();
            ex.printStackTrace();
        }
    }

    public void executePostsByTag() throws IOException {

        if (securityComponent.getAuthenticationUser(userIp) == null) {
            output.append("Для получения списка постов по тегу необходимо войти в систему.").append("\n").flush();
            return;
        }

        output.append("<<<< Поиск публикаций по тегу >>>>").append("\n").flush();

        output.append("Введите тег: ").flush();
        String tag = this.input.readLine();

        if (tag.trim().isEmpty()) {
            output.append("Тег не может быть пустым.").append("\n").flush();
            return;
        }

        try {
            List<Post> postsByTag = postService.getPostsByTag(tag);

            for (Post post : postsByTag) {
                // Здесь нужно получить автора для каждого поста
                User author = userService.getUserById(post.getAuthorId());
                String postAsString = postViewMapper.postToBeautifulString(post, author.getLogin());
                output.append(postAsString).append("\n").flush();

            }

            output.append("<<<< Конец всех публикаций >>>>").append("\n").flush();

        } catch (PostNotFoundException ex) {
            output.append(ex.getMessage()).append("\n").flush();
        } catch (Exception ex) {
            output.append("Произошла ошибка: " + ex.getMessage() + "\n").flush();
            ex.printStackTrace();
        }
    }

    public void executePostsByUserLogin() throws IOException {

        if (securityComponent.getAuthenticationUser(userIp) == null) {
            output.append("Для получения списка постов по логину пользователя, необходимо войти в систему.").append("\n").flush();
            return;
        }

        output.append("<<<< Публикации по логину пользователя >>>>").append("\n").flush();

        output.append("Введите логин пользователя: ").flush();
        String login = this.input.readLine();

        if (login.trim().isEmpty()) {
            output.append("Логин не может быть пустым.").append("\n").flush();
            return;
        }

        try {
            User user = userService.getUserByLogin(login);

            List<Post> postsByUserLogin = postService.getPostsByUser(user);

            if (postsByUserLogin.isEmpty()) {
                throw new PostNotFoundException("У пользователя " + user.getLogin() + " нет постов.");
            }

            for (Post post : postsByUserLogin) {
                if (post != null && post.getAuthorId() == user.getId()) {

                    String postAsString = postViewMapper.postToBeautifulString(post, user.getLogin());
                    output.append(postAsString).append("\n").flush();

                }
            }

            output.append("<<<< Конец публикаций >>>>").append("\n").flush();

        } catch (UserNotFoundException | PostNotFoundException ex) {
            output.append(ex.getMessage()).append("\n").flush();
        } catch (Exception ex) {
            output.append("Произошла ошибка: " + ex.getMessage() + "\n").flush();
            ex.printStackTrace();
        }
    }

    public void executeAllPostsByUserType() throws IOException {
        if (securityComponent.getAuthenticationUser(userIp) == null) {
            output.append("Для получения списка постов по типу пользователя, необходимо войти в систему.").append("\n").flush();
            return;
        }

        output.append("<<<< Публикации по типу пользователя: >>>>").append("\n").flush();

        int intType = -1;

        while (intType != 0 && intType != 1) {
            try {
                output.append("Введите тип пользователя(0 - человек, 1 - организация): ").flush();
                String input = this.input.readLine();

                if (input.trim().isEmpty()) {
                    output.append("Тип пользователя не может быть пустым.").append("\n").flush();
                    continue;
                }

                intType =  Integer.parseInt(input);

                if (intType != 0 && intType != 1) {
                    output.append("Введен некорректный тип пользователя.").append("\n").flush();
                }
            } catch (InputMismatchException ex) {
                output.append("Введите числовое значение.").append("\n").flush();
            }
        }

        try {
            UserType userType = UserType.getUserType(intType);

            List<Post> postsByUserType = postService.getPostsByUserType(userType);
            for (Post post : postsByUserType) {

                User author = userService.getUserById(post.getAuthorId());
                String postAsString = postViewMapper.postToBeautifulString(post, author.getLogin());
                output.append(postAsString).append("\n").flush();

            }

            output.append("<<<< Конец публикаций >>>>").append("\n").flush();

        } catch (UserNotFoundException | PostNotFoundException | UnknownUserTypeException ex) {
            output.append(ex.getMessage()).append("\n").flush();
        } catch (Exception ex) {
            output.append("Произошла ошибка: " + ex.getMessage() + "\n").flush();
            ex.printStackTrace();
        }
    }

    public void executeDeletePost() throws IOException {
        User currentUser = securityComponent.getAuthenticationUser(userIp);
        if (currentUser == null) {
            output.append("Для удаления поста необходимо войти в систему.\n").flush();
            return;
        }

        output.append("<<<< Удаление публикации >>>>\n").flush();
        output.append("Введите ID поста, который хотите удалить: ").flush();

        try {
            String inputId = input.readLine();
            if (inputId == null || inputId.trim().isEmpty()) {
                output.append("ID поста не может быть пустым.\n").flush();
                return;
            }

            Long postId = Long.parseLong(inputId.trim());

            // 1. Находим пост
            Post postToDelete = postService.getPostById(postId);

            // 2. Проверяем права: только автор может удалить свой пост
            if (postToDelete.getAuthorId() != currentUser.getId()) {
                output.append("Ошибка: вы не можете удалить чужую публикацию.\n").flush();
                return;
            }

            // 3. Удаляем пост
            postService.deletePost(postToDelete);

            output.append("Публикация с ID " + postId + " успешно удалена.\n").flush();

        } catch (NumberFormatException e) {
            output.append("Ошибка: введите корректное числовое значение для ID.\n").flush();
        } catch (PostNotFoundException e) {
            output.append("Ошибка: " + e.getMessage() + "\n").flush();
        } catch (Exception e) {
            output.append("Произошла непредвиденная ошибка при удалении поста.\n").flush();
            e.printStackTrace();
        }

        output.append("<<<< Конец удаления >>>>\n").flush();
    }

}
