package twitter.mapper.v1.fileMapper.impl;

import twitter.configuration.Component;
import twitter.configuration.Injection;
import twitter.configuration.Profile;
import twitter.entity.post.Post;
import twitter.entity.user.User;
import twitter.exceptions.DataAccessException;
import twitter.exceptions.UserNotFoundException;
import twitter.mapper.v1.fileMapper.FilePostMapper;
import twitter.dto.v1.PostDto;
import twitter.service.UserService;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@Profile(active = "test")
public class FilePostMapperImpl implements FilePostMapper {

    private final UserService userService;

    @Injection
    public FilePostMapperImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public Post buildPostFromFile(String lineData) {
        Post post = new Post();

        try {

            String temp = lineData.substring(6);

            String author = temp.substring(1, temp.indexOf("}"));
            String tempVariable1 = temp.substring(temp.indexOf("}") + 1);

            String postId = tempVariable1.substring(1, tempVariable1.indexOf("}"));
            String tempVariable2 = tempVariable1.substring(tempVariable1.indexOf("}") + 1);

            String topic =  tempVariable2.substring(1, tempVariable2.indexOf("}"));
            String tempVariable3 = tempVariable2.substring(tempVariable2.indexOf("}") + 1);

            String text = tempVariable3.substring(1, tempVariable3.indexOf("}"));
            String tempVariable4 = tempVariable3.substring(tempVariable3.indexOf("}") + 1);

            String tags = tempVariable4.substring(1, tempVariable4.indexOf("}"));
            if (!tags.isEmpty()) {
                String cleanTags = tags.replace("#", "");
                String[] tagsArray = cleanTags.split(",");
                for (int i = 0; i < tagsArray.length; i++) {
                    tagsArray[i] = tagsArray[i].trim();
                }
                post.setTags(tagsArray);
            }
            String tempVariable5 =  tempVariable4.substring(tempVariable4.indexOf("}") + 1);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime createdAt = LocalDateTime.parse(tempVariable5.substring(1, tempVariable5.indexOf("}")), formatter);

            User user = userService.getUserById(Integer.parseInt(author));

            post.setAuthor(user);
            post.setPostId(Integer.parseInt(postId));
            post.setTopic(topic);
            post.setText(text);
            post.setCreatedAt(createdAt);

        } catch (Exception e) {
            System.err.println("Ошибка парсинга строки " + lineData);
            e.printStackTrace();
        }
        return post;
    }

    @Override
    public PostDto postResponseDto(Post post) throws UserNotFoundException {
        PostDto postDto = new PostDto();

        postDto.setCreatedAt(post.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        postDto.setTopic(post.getTopic());
        postDto.setText(post.getText());
        postDto.setTags("Тегов не найдено");

        if (post.getTags() != null && post.getTags().length > 0) {
            StringBuilder sb = new StringBuilder();
            for (String tag : post.getTags()) {
                sb.append("#").append(tag.trim()).append(", ");
            }
            sb.setLength(sb.length() - 2);
            postDto.setTags(sb.toString());
        }

        postDto.setAuthor(post.getAuthor().userName());

        return postDto;
    }

    @Override
    public Post getPostFromDB(ResultSet resultSet) throws SQLException {

        Post post = new Post();

        try {
            User author = userService.getUserById(resultSet.getInt("author_id"));

            post.setAuthor(author);
            post.setPostId(resultSet.getInt("post_id"));
            post.setTopic(resultSet.getString("topic"));
            post.setText(resultSet.getString("text"));
            post.setCreatedAt(resultSet.getTimestamp("created_at").toLocalDateTime());

        } catch (UserNotFoundException ex) {
            throw new DataAccessException(ex.getMessage());
        }

        String tagsFromDB = resultSet.getString("tags");

        if (tagsFromDB != null && !tagsFromDB.isEmpty()) {

            String[] tagsArray = tagsFromDB.split(",");

            for (int i = 0; i < tagsArray.length; i++) {
                tagsArray[i] = tagsArray[i].trim();
            }

            post.setTags(tagsArray);

        } else {
            post.setTags(new String[0]);
        }

        return post;
    }

}
