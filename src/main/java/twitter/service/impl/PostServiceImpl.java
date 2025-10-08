package twitter.service.impl;

import twitter.configuration.Component;
import twitter.configuration.Injection;
import twitter.dao.PostDAO;
import twitter.entity.post.Post;
import twitter.entity.user.User;
import twitter.entity.user.UserType;
import twitter.exceptions.PostNotFoundException;
import twitter.exceptions.UserNotFoundException;
import twitter.service.PostService;
import twitter.service.UserService;

import java.util.*;

@Component
public class PostServiceImpl implements PostService {

    private final PostDAO postDAO;
    private final UserService userService;

    @Injection
    public PostServiceImpl(
            PostDAO postDAO,
            UserService userService
    ) {
        this.postDAO = postDAO;
        this.userService = userService;
    }

    @Override
    public Post savePost(Post post) throws PostNotFoundException {
        return postDAO.saveNewPost(post);
    }

    @Override
    public List<Post> getPostsByUser(User user) throws PostNotFoundException {
        List<Post> userPosts = postDAO.getAllPostsByUser(user.getId());

        return userPosts.stream()
                .sorted(Comparator.comparing(Post::getCreatedAt).reversed())
                .toList();
    }

    @Override
    public List<Post> getAllPosts() throws PostNotFoundException {

        List<Post> allPosts = postDAO.getAllPosts();

        if (allPosts.isEmpty()) {
            throw new PostNotFoundException();
        }

        return allPosts.stream()
                .sorted(Comparator.comparing(Post::getCreatedAt).reversed())
                .toList();
    }

    @Override
    public List<Post> getPostsByTag(String tag) throws PostNotFoundException {

        List<Post> postsByTag = postDAO.getAllPostsByTag(tag);

        if (postsByTag.isEmpty()) {
            throw new PostNotFoundException("Не удалось найти публикации по тегу: #" + tag);
        }

        return postsByTag;
    }

    @Override
    public List<Post> getPostsByUserType(UserType userType) throws PostNotFoundException, UserNotFoundException {
        List<User> usersByType = userService.getAllUsersByUserType(userType);

        if (usersByType.isEmpty()) {
            throw new UserNotFoundException();
        }

        Long[] userIds = new Long[usersByType.size()];
        for (int i = 0; i < usersByType.size(); i++) {
            userIds[i] = usersByType.get(i).getId();
        }

        return postDAO.getAllPostsByUserIdIn(userIds);
    }

    @Override
    public Post getPostById(Long postId) throws PostNotFoundException {
        return postDAO.getPostById(postId);
    }

    @Override
    public void deletePost(Post post) throws PostNotFoundException {
        postDAO.deletePost(post);
    }
}
