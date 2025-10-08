package twitter.dao.impl;

import twitter.configuration.Component;
import twitter.configuration.Injection;
import twitter.configuration.Profile;
import twitter.dao.PostDAO;
import twitter.entity.post.Post;
import twitter.exceptions.PostNotFoundException;
import twitter.mapper.fileMapper.FilePostMapper;

import java.io.*;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

@Component
@Profile(active = "test")
public class FilePostDAO implements PostDAO{

    private final List<Post> posts;
    private Integer postId;

    private final FilePostMapper postMapper;

    @Injection
    public FilePostDAO(FilePostMapper postMapper) {
        this.postMapper = postMapper;
        posts = new LinkedList<>();
        getFromFile();
    }

    public synchronized void saveToFile(Post post) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("postsData.txt", true))) {
            writer.write(post.toFile());
            writer.newLine();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private synchronized void getFromFile() {
        int maxId = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader("postsData.txt"))) {

            while (reader.ready()) {
                String line = reader.readLine();
                Post postFromFile = this.postMapper.buildPostFromFile(line);
                posts.add(postFromFile);

                if (postFromFile.getPostId() > maxId) {
                    maxId = postFromFile.getPostId();
                }
            }

        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            System.exit(1);
        }
        postId = maxId;
    }

    @Override
    public synchronized Post saveNewPost(Post post) throws PostNotFoundException {
        if (this.postId == null) {
            throw new PostNotFoundException("Не удалось сохранить публикацию");
        }

        postId = postId + 1;
        post.setPostId(postId);
        post.setCreatedAt(LocalDateTime.now());
        posts.add(post);

        saveToFile(post);

        return post;
    }

    @Override
    public synchronized List<Post> getAllPosts() throws PostNotFoundException {
        if (this.posts.isEmpty()) {
            throw new PostNotFoundException("Не удалось найти публикации");
        }

        return this.posts;
    }

    @Override
    public synchronized List<Post> getAllPostsByUser(Long userId) throws PostNotFoundException {
        if (this.posts.isEmpty()) {
            throw new PostNotFoundException("Не удалось найти публикации");
        }

        return this.posts
                .stream()
                .filter(post -> userId.equals((long) post.getAuthorId()))
                .toList();
    }

    @Override
    public synchronized List<Post> getAllPostsByTag(String tag) throws PostNotFoundException {
        if (this.posts.isEmpty()) {
            throw new PostNotFoundException("Не удалось найти публикации");
        }

        return this.posts
                .stream()
                .filter(post -> post.hasTag(tag))
                .toList();
    }

    @Override
    public synchronized List<Post> getAllPostsByUserIdIn(Long[] userIds) throws PostNotFoundException {
        if (this.posts.isEmpty()) {
            throw new PostNotFoundException("Не удалось найти публикации");
        }

        return this.posts
                .stream()
                .filter(post -> isUserIdInArray(post.getAuthorId(), userIds))
                .toList();
    }

    public synchronized boolean isUserIdInArray(int userId, Long[] userIds) {
        for (Long id : userIds) {
            if (id == userId) {
                return true;
            }
        }
        return false;
    }

    @Override
    public synchronized void deletePost(Post post) {
        posts.remove(post);
    }
    
    @Override
    public Post getPostById(Long postId) throws PostNotFoundException {
        return posts.stream()
                .filter(p -> p.getPostId().equals(postId.intValue()))
                .findFirst()
                .orElseThrow(() -> new PostNotFoundException("Публикация с ID " + postId + " не найдена."));
    }
}
