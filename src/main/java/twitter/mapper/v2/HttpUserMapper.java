package twitter.mapper.v2;

import twitter.dto.v2.response.InfoResponseDto;
import twitter.entity.user.User;

public interface HttpUserMapper {

    InfoResponseDto mapUserToResponseDto(User user);

}
