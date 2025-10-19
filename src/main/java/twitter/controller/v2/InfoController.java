package twitter.controller.v2;

import twitter.configuration.Component;
import twitter.configuration.Injection;
import twitter.dto.v2.response.InfoResponseDto;
import twitter.entity.user.Organization;
import twitter.entity.user.Person;
import twitter.entity.user.User;
import twitter.entity.user.UserType;
import twitter.exceptions.TwitterIllegalArgumentException;
import twitter.exceptions.UserNotFoundException;
import twitter.service.UserService;

import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Component
public class InfoController {

    private final UserService userService;

    @Injection
    public InfoController(UserService userService) {
        this.userService = userService;
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

            InfoResponseDto responseDto = new InfoResponseDto();
            responseDto.setId(user.getId());
            responseDto.setLogin(user.getLogin());

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
            if (UserType.PERSON.equals(user.getUserType())) {
                Person person = (Person) user;
                responseDto.setName(person.getName());
                responseDto.setLastName(person.getLastName());
                responseDto.setBirthDate(person.getBirthDate().format(formatter));
            }

            if (UserType.ORGANIZATION.equals(user.getUserType())) {
                Organization organization = (Organization) user;
                responseDto.setTitle(organization.getTitle());
                responseDto.setSpecialization(organization.getSpecialization());
                responseDto.setDateOfFoundation(organization.getDateOfFoundation().format(formatter));
            }

            return responseDto;
        } catch (UserNotFoundException ex) {
            throw new TwitterIllegalArgumentException(ex.getMessage());
        }

    }

}
