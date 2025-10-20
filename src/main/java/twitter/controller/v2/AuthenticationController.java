package twitter.controller.v2;

import org.springframework.security.crypto.password.PasswordEncoder;
import twitter.configuration.Component;
import twitter.configuration.Injection;
import twitter.dto.v2.request.LoginRequestDto;
import twitter.dto.v2.response.LoginResponseDto;
import twitter.entity.user.User;
import twitter.exceptions.TwitterIllegalArgumentException;
import twitter.exceptions.UserNotFoundException;
import twitter.security.JwtHandler;
import twitter.service.UserService;

import java.util.Objects;

@Component
public class AuthenticationController {

    private final UserService userService;
    private final JwtHandler jwtHandler;
    private final PasswordEncoder passwordEncoder;

    @Injection
    public AuthenticationController(UserService userService, JwtHandler jwtHandler, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.jwtHandler = jwtHandler;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponseDto login(LoginRequestDto request) {

            if (Objects.isNull(request.getLogin()) || request.getLogin().isEmpty()) {
                throw new TwitterIllegalArgumentException("Логин не может быть пустым.");
            }

            if (request.getLogin().contains(" ")) {
                throw new TwitterIllegalArgumentException("Логин не может содержать пробелы.");
            }

        try {

            if (!userService.isUserExists(request.getLogin())) {
                throw new TwitterIllegalArgumentException("Не удалось найти пользователя с таким логином.");
            }

            User user = userService.getUserByLogin(request.getLogin());

            String hashedPassword = user.getPassword();

            if (passwordEncoder.matches(request.getPassword(), hashedPassword)) {
                throw new TwitterIllegalArgumentException("Введен неверный пароль.");
            }

            String token = this.jwtHandler.generateToken(request.getLogin());

            LoginResponseDto loginResponseDto = new LoginResponseDto();
            loginResponseDto.setToken(token);

            return loginResponseDto;
        } catch (UserNotFoundException ex) {
            throw new TwitterIllegalArgumentException("Не удалось найти пользователя с логином: " + request.getLogin());
        }
    }
}