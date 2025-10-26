package twitter.mapper.v1.fileMapper;

import twitter.entity.post.Post;
import twitter.exceptions.UserNotFoundException;
import twitter.dto.v1.PostDto;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface FilePostMapper {

    Post buildPostFromFile(String lineData);

    PostDto postResponseDto(Post post) throws UserNotFoundException;

    Post getPostFromDB(ResultSet resultSet) throws SQLException;



}
