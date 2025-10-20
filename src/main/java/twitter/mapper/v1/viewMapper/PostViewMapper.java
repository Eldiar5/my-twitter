package twitter.mapper.v1.viewMapper;

import twitter.entity.post.Post;

public interface PostViewMapper {

    String postToBeautifulString(Post post, String authorLogin);

}
