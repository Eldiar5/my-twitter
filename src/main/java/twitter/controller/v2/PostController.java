package twitter.controller.v2;

import twitter.configuration.Component;
import twitter.configuration.Injection;
import twitter.dto.v2.request.PostRequestDto;
import twitter.dto.v2.response.PostResponseDto;
import twitter.entity.post.Post;
import twitter.entity.user.User;
import twitter.exceptions.TwitterIllegalArgumentException;
import twitter.exceptions.UserNotFoundException;
import twitter.service.PostService;
import twitter.service.UserService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;

@Component
public class PostController {

    private final PostService postService;
    private final UserService userService;

    @Injection
    public PostController(PostService postService, UserService userService) {
        this.postService = postService;
        this.userService = userService;
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

            post.setAuthorId(user.getId().intValue());
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

    private void validateField(String field, String errorMessage) {
        if (Objects.isNull(field) || field.trim().isEmpty()) {
            throw new TwitterIllegalArgumentException(errorMessage);
        }
    }
}
