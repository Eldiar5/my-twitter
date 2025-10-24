package twitter.controller.v2;

import twitter.configuration.Component;
import twitter.configuration.Injection;
import twitter.configuration.Profile;
import twitter.dto.v2.response.InfoResponseDto;
import twitter.entity.user.User;
import twitter.entity.user.UserType;
import twitter.exceptions.TwitterIllegalArgumentException;
import twitter.exceptions.UserNotFoundException;
import twitter.mapper.v2.HttpUserMapper;
import twitter.service.UserService;

import java.util.List;
import java.util.Objects;

@Component
@Profile(active = {"default", "prod"})
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
            throw new TwitterIllegalArgumentException("Некорректное имя");
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

    public InfoResponseDto infoByLogin(String login) {
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

    public List<InfoResponseDto> infoAllByUserType(UserType userType) {
        if (Objects.isNull(userType)) {
            throw new TwitterIllegalArgumentException("Некорректный тип пользователя.");
        }

        try {
            return this.userService.getAllUsersByUserType(userType)
                    .stream()
                    .map(userMapper::mapUserToResponseDto)
                    .toList();

        } catch (UserNotFoundException ex) {
            throw new TwitterIllegalArgumentException(ex.getMessage());
        }
    }

    public List<InfoResponseDto> infoAll() {
        try {

            if (userService.getAllUsers().isEmpty()) {
                throw new TwitterIllegalArgumentException("Список пользователей пуст.");
            }

            return this.userService.getAllUsers()
                    .stream()
                    .map(userMapper::mapUserToResponseDto)
                    .toList();

        } catch (UserNotFoundException ex) {
            throw new TwitterIllegalArgumentException(ex.getMessage());
        }
    }

}
