package twitter.mapper.v2.impl;

import twitter.configuration.Component;
import twitter.configuration.Profile;
import twitter.dto.v2.response.PostResponseDto;
import twitter.entity.post.Post;
import twitter.entity.user.User;
import twitter.mapper.v2.HttpPostMapper;

import java.util.Arrays;

@Component
@Profile(active = {"default", "prod"})
public class HttpPostMapperImpl implements HttpPostMapper {

    @Override
    public PostResponseDto mapPostToResponseDto(Post post, User author) {
        PostResponseDto postResponseDto = new PostResponseDto();

        postResponseDto.setAuthorLogin(author.getLogin());

        postResponseDto.setPostId(post.getPostId());
        postResponseDto.setTopic(post.getTopic());
        postResponseDto.setText(post.getText());
        postResponseDto.setTags(Arrays.asList(post.getTags()));

        postResponseDto.setCreatedAt(post.getCreatedAt());

        return postResponseDto;
    }
}
