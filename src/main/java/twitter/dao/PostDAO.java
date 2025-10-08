package twitter.dao;

import twitter.entity.post.Post;
import twitter.exceptions.PostNotFoundException;

import java.util.List;

public interface PostDAO {

    Post saveNewPost(Post post) throws PostNotFoundException;

    List<Post> getAllPosts() throws PostNotFoundException ;
    List<Post> getAllPostsByUser(Long userId) throws PostNotFoundException ;
    List<Post> getAllPostsByTag(String tag) throws PostNotFoundException ;
    List<Post> getAllPostsByUserIdIn(Long[] userIds) throws PostNotFoundException ;

    void deletePost(Post post);

    Post getPostById(Long postId) throws PostNotFoundException;

}
