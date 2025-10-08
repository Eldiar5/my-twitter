package twitter.mapper.fileMapper;

import twitter.entity.post.Post;
import twitter.exceptions.UserNotFoundException;
import twitter.postDto.PostDto;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface FilePostMapper {

    Post buildPostFromFile(String lineData);

    PostDto postResponseDto(Post post) throws UserNotFoundException;

    Post getPostFromDB(ResultSet resultSet) throws SQLException;



}
