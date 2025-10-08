package twitter.service;

import twitter.entity.post.Post;
import twitter.entity.user.User;
import twitter.entity.user.UserType;
import twitter.exceptions.PostNotFoundException;
import twitter.exceptions.UserNotFoundException;

import java.util.List;

public interface PostService {

    Post savePost(Post post) throws PostNotFoundException;

    List<Post> getPostsByUser(User user) throws PostNotFoundException;

    List<Post> getAllPosts() throws PostNotFoundException;

    List<Post> getPostsByTag(String tag) throws PostNotFoundException;

    List<Post> getPostsByUserType(UserType userType) throws PostNotFoundException, UserNotFoundException;

    Post getPostById(Long postId) throws PostNotFoundException;
    void deletePost(Post post) throws PostNotFoundException;

}
