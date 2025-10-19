package twitter.controller.v2;

import twitter.configuration.Component;
import twitter.configuration.Injection;
import twitter.dto.v2.request.RegistrationRequestDto;
import twitter.dto.v2.response.RegistrationResponseDto;
import twitter.entity.user.Organization;
import twitter.entity.user.Person;
import twitter.entity.user.User;
import twitter.entity.user.UserType;
import twitter.exceptions.TwitterIllegalArgumentException;
import twitter.exceptions.UnknownUserTypeException;
import twitter.exceptions.UserNotFoundException;
import twitter.service.UserService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Component
public class RegistrationController {

    private final UserService  userService;

    @Injection
    public RegistrationController(UserService userService) {
        this.userService = userService;
    }

    public RegistrationResponseDto register(RegistrationRequestDto requestDto) {
        if (Objects.isNull(requestDto.getUserType())) {
            throw new IllegalArgumentException("Тип пользователя не может быть пустым.");
        }
        if (requestDto.getUserType() != 0 && requestDto.getUserType() != 1) {
            throw new IllegalArgumentException("Введен некорректный тип пользователя.");
        }
        if (Objects.isNull(requestDto.getLogin()) || requestDto.getLogin().isEmpty() || requestDto.getLogin().length() < 3) {
            throw new IllegalArgumentException("Логин не может быть пустым или менее 3 символов.");
        }
        if (Objects.isNull(requestDto.getPassword()) || requestDto.getPassword().isEmpty() || requestDto.getPassword().length() < 5) {
            throw new IllegalArgumentException("Пароль не может быть пустым или менее 5 символов.");
        }
        if (requestDto.getPassword().contains(" ")) {
            throw new IllegalArgumentException("Пароль не может содержать пробелы.");
        }

        try {
            if (userService.isUserExists(requestDto.getLogin())) {
                throw new IllegalArgumentException("Пользователь под таким логином уже есть.");
            }

            UserType userType = UserType.getUserType(requestDto.getUserType());
            DateTimeFormatter formatter =  DateTimeFormatter.ofPattern("yyyy-MM-dd");

            if (UserType.PERSON.equals(userType)) {
                Person user = new Person();

                validateField(requestDto.getName(), "Имя не может быть пустым.");
                validateField(requestDto.getLastName(), "Фамилия не может быть пустой.");
                LocalDate birthDate = validateDate(requestDto.getBirthDate(), "рождения");

                if (birthDate.isBefore(LocalDate.now().minusYears(100))) {
                    throw new TwitterIllegalArgumentException("Дата рождения не может превышать 100 лет.");
                }

                user.setUserType(userType);
                user.setLogin(requestDto.getLogin());
                user.setPassword(requestDto.getPassword());
                user.setName(requestDto.getName());
                user.setLastName(requestDto.getLastName());
                user.setBirthDate(birthDate);

                User savedUser = userService.saveNewUser(user);

                RegistrationResponseDto responseDto = new RegistrationResponseDto();
                responseDto.setId(savedUser.getId());
                responseDto.setLogin(savedUser.getLogin());
                responseDto.setName(user.getName());
                responseDto.setLastName(user.getLastName());
                responseDto.setBirthDate(user.getBirthDate().format(formatter));

                return responseDto;

            } else {
                Organization user = new Organization();

                validateField(requestDto.getTitle(), "Название организации не может быть пустым");
                validateField(requestDto.getSpecialization(), "Род деятельности не может быть пустым");
                LocalDate dateOfFoundation = validateDate(requestDto.getDateOfFoundation(), "основания");

                if (dateOfFoundation.isBefore(LocalDate.now().minusYears(225))) {
                    throw new TwitterIllegalArgumentException("Дата основания не может превышать 1800 год.");
                }

                user.setUserType(userType);
                user.setLogin(requestDto.getLogin());
                user.setPassword(requestDto.getPassword());
                user.setTitle(requestDto.getTitle());
                user.setSpecialization(requestDto.getSpecialization());
                user.setDateOfFoundation(dateOfFoundation);

                User savedUser = userService.saveNewUser(user);

                RegistrationResponseDto responseDto = new RegistrationResponseDto();
                responseDto.setLogin(savedUser.getLogin());
                responseDto.setId(savedUser.getId());
                responseDto.setTitle(user.getTitle());
                responseDto.setSpecialization(user.getSpecialization());
                responseDto.setDateOfFoundation(user.getDateOfFoundation().format(formatter));

                return responseDto;
            }

        } catch (UserNotFoundException | UnknownUserTypeException ex) {
            throw new TwitterIllegalArgumentException(ex.getMessage());
        }
    }

    private void validateField(String field, String errorMessage) {
        if (Objects.isNull(field) || field.trim().isEmpty()) {
            throw new TwitterIllegalArgumentException(errorMessage);
        }
    }

    private LocalDate validateDate(String date, String dateType) {
        if (Objects.isNull(date) || date.trim().isEmpty()) {
            throw new TwitterIllegalArgumentException("Дата " + dateType + " не может быть пустой");
        }
        String regex = "^\\d{4}-\\d{2}-\\d{2}$";
        if (!date.matches(regex)) {
            throw new TwitterIllegalArgumentException("Неверно введена дата " + dateType);
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate parsedDate = LocalDate.parse(date, formatter);
        if (parsedDate.isAfter(LocalDate.now())) {
            throw new TwitterIllegalArgumentException("Дата " + dateType + " не может быть в будущем");
        }
        return parsedDate;
    }

}
