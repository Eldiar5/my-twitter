package twitter.controller.v2;

import twitter.configuration.Component;
import twitter.configuration.Injection;
import twitter.configuration.Profile;
import twitter.dto.v2.request.PostRequestDto;
import twitter.dto.v2.response.PostResponseDto;
import twitter.entity.post.Post;
import twitter.entity.user.User;
import twitter.entity.user.UserType;
import twitter.exceptions.PostNotFoundException;
import twitter.exceptions.TwitterIllegalArgumentException;
import twitter.exceptions.UserNotFoundException;
import twitter.mapper.v2.HttpPostMapper;
import twitter.service.PostService;
import twitter.service.UserService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Component
@Profile(active = {"default", "prod"})
public class PostController {

    private final PostService postService;
    private final UserService userService;
    private final HttpPostMapper postMapper;

    @Injection
    public PostController(PostService postService, UserService userService, HttpPostMapper postMapper) {
        this.postService = postService;
        this.userService = userService;
        this.postMapper = postMapper;
    }

    public PostResponseDto addPost(PostRequestDto postRequestDto, String author) {
        this.validateField(postRequestDto.getTopic(), "Тема публикации не может быть пустой.");

        this.validateField(postRequestDto.getText(), "Текст публикации не может быть пустой.");

        try {
            User user = userService.getUserByLogin(author);

            Post post = new Post();

            post.setTopic(postRequestDto.getTopic());
            post.setText(postRequestDto.getText());

            if (!Objects.isNull(postRequestDto)) {
                post.setTags(postRequestDto.getTags().toArray(new String[0]));
            } else {
                post.setTags(new String[0]);
            }

            post.setAuthor(user);
            post.setCreatedAt(LocalDateTime.now());

            Post savedPost = postService.savePost(post);

            PostResponseDto postResponseDto = new PostResponseDto();
            postResponseDto.setAuthorLogin(user.getLogin());
            postResponseDto.setPostId(savedPost.getPostId());
            postResponseDto.setTopic(savedPost.getTopic());
            postResponseDto.setText(savedPost.getText());
            postResponseDto.setTags(Arrays.asList(savedPost.getTags()));
            postResponseDto.setCreatedAt(savedPost.getCreatedAt());

            return postResponseDto;
        } catch (UserNotFoundException ex) {
            throw new TwitterIllegalArgumentException(ex.getMessage());
        } catch (Exception ex) {
            throw new TwitterIllegalArgumentException("Не удалось создать пост: " + ex.getMessage());
        }
    }

    public void deletePost(Long postId, String authorLogin) {
        if (Objects.isNull(postId)) {
            throw new TwitterIllegalArgumentException("ID поста не может быть пустым.");
        }

        try {

            User postAuthor = userService.getUserByLogin(authorLogin);

            if (Objects.isNull(postAuthor)) {
                throw new UserNotFoundException("Не удалось найти автора: " + authorLogin);
            }

            Post postToDelete = postService.getPostById(postId);

            if (!Objects.equals(postToDelete.getAuthor().getId(), postAuthor.getId())) {
                throw new SecurityException("Ошибка: вы не можете удалить чужую публикацию.");
            }

            postService.deletePost(postToDelete);

        } catch (UserNotFoundException | PostNotFoundException ex) {
            throw new TwitterIllegalArgumentException(ex.getMessage());
        }

    }

    public List<PostResponseDto> myPosts(String login) {
        this.validateField(login, "Некорректное имя");

        try {

            User postAuthor = userService.getUserByLogin(login);

            if (Objects.isNull(postAuthor)) {
                throw new UserNotFoundException("Не удалось найти автора: " + login);
            }

            List<Post> posts = postService.getPostsByUser(postAuthor);

            return posts.stream()
                    .map(post -> postMapper.mapPostToResponseDto(post, postAuthor))
                    .toList();

        } catch (UserNotFoundException | PostNotFoundException ex) {
            throw new TwitterIllegalArgumentException(ex.getMessage());
        }
    }

    public List<PostResponseDto> allPosts() {
        try {

            List<Post> allPosts = postService.getAllPosts();

            if (Objects.isNull(allPosts)) {
                throw new PostNotFoundException("Список публикаций пуст.");
            }

            return allPosts.stream()
                    .map(post -> postMapper.mapPostToResponseDto(post, post.getAuthor()))
                    .toList();

        } catch (PostNotFoundException ex) {
            throw new TwitterIllegalArgumentException(ex.getMessage());
        }
    }

    public List<PostResponseDto> postsByTag(String tag) {
        this.validateField(tag, "Тег не может быть пустым.");

        try {
            List<Post> postsByTag = postService.getPostsByTag(tag);

            if (Objects.isNull(postsByTag)) {
                throw new PostNotFoundException("Не удалось найти публикации по тегу " + tag);
            }

            return postsByTag.stream()
                    .map(post -> postMapper.mapPostToResponseDto(post, post.getAuthor()))
                    .toList();

        } catch (PostNotFoundException ex) {
            throw new TwitterIllegalArgumentException(ex.getMessage());
        }
    }

    public List<PostResponseDto> postsByLogin(String login) {
        this.validateField(login, "Логин не может быть пустым.");

        try {
            User author = userService.getUserByLogin(login);

            if (Objects.isNull(author)) {
                throw new UserNotFoundException("Не удалось найти пользователя по логину: " + login);
            }

            List<Post> postsByLogin = postService.getPostsByUser(author);

            if (Objects.isNull(postsByLogin)) {
                throw new PostNotFoundException("Не удалось найти публикации автора по логину: " + login );
            }

            return postsByLogin.stream()
                    .map(post -> postMapper.mapPostToResponseDto(post, post.getAuthor()))
                    .toList();

        } catch (PostNotFoundException | UserNotFoundException ex) {
            throw new TwitterIllegalArgumentException(ex.getMessage());
        }
    }

    public List<PostResponseDto> postsByUserType(UserType userType) {
        if (Objects.isNull(userType)) {
            throw new TwitterIllegalArgumentException("Некорректный тип пользователя.");
        }

        try {
            List<Post> postsByUserType = postService.getPostsByUserType(userType);

            if (Objects.isNull(postsByUserType)) {
                throw new PostNotFoundException("Не удалось найти публикации по типу пользователя: " + userType);
            }

            return postsByUserType.stream()
                    .map(post -> postMapper.mapPostToResponseDto(post, post.getAuthor()))
                    .toList();

        } catch (PostNotFoundException | UserNotFoundException ex) {
            throw new TwitterIllegalArgumentException(ex.getMessage());
        }
    }

    private void validateField(String field, String errorMessage) {
        if (Objects.isNull(field) || field.trim().isEmpty()) {
            throw new TwitterIllegalArgumentException(errorMessage);
        }
    }
}
