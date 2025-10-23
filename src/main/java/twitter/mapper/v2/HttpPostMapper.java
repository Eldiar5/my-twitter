package twitter.mapper.v2;

import twitter.dto.v2.response.PostResponseDto;
import twitter.entity.post.Post;
import twitter.entity.user.User;

public interface HttpPostMapper {

    PostResponseDto mapPostToResponseDto(Post post, User author);

}
