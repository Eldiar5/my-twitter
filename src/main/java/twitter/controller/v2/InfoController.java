package twitter.controller.v2;

import twitter.configuration.Component;
import twitter.configuration.Injection;
import twitter.dto.v2.response.InfoResponseDto;
import twitter.entity.user.User;
import twitter.exceptions.TwitterIllegalArgumentException;
import twitter.exceptions.UserNotFoundException;
import twitter.mapper.v2.HttpUserMapper;
import twitter.service.UserService;

import java.util.Objects;

@Component
public class InfoController {

    private final UserService userService;
    private final HttpUserMapper userMapper;

    @Injection
    public InfoController(UserService userService, HttpUserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    public InfoResponseDto info(String login) {
        if (Objects.isNull(login) ||  login.isBlank()) {
            throw new TwitterIllegalArgumentException("Некорректное имя пользователя");
        }

        try {
            User user = userService.getUserByLogin(login);

            if (Objects.isNull(user)) {
                throw new UserNotFoundException("Не удалось найти пользователя с логином: " + login);
            }

            return userMapper.mapUserToResponseDto(user);
        } catch (UserNotFoundException ex) {
            throw new TwitterIllegalArgumentException(ex.getMessage());
        }
    }

}
