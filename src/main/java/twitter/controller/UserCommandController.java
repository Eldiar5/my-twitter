package twitter.controller;

import twitter.entity.user.UserType;
import twitter.exceptions.ClientDisconnectedException;
import twitter.exceptions.UnknownUserTypeException;
import twitter.exceptions.UserNotFoundException;
import twitter.factory.command.TwitterCommandEnum;
import twitter.security.Security;
import twitter.service.UserService;
import twitter.entity.user.Organization;
import twitter.entity.user.Person;
import twitter.entity.user.User;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.InputMismatchException;
import java.util.List;

//@Component
public class UserCommandController {

    private final UserService userService;
    private final Security securityComponent;
    private final BufferedReader input;
    private final BufferedWriter output;
    private final String userIp;


//    @Injection
    public UserCommandController(
            UserService userService,
            Security securityComponent,
            BufferedReader input,
            BufferedWriter output, String userIp
    ) {
        this.userService = userService;
        this.securityComponent = securityComponent;
        this.input = input;
        this.output = output;
        this.userIp = userIp;
    }

    public void executeHelp() throws IOException {
        output.append("<<<<<<<< Системные команды: >>>>>>>>").append("\n").flush();
        output.write(TwitterCommandEnum.getAllCommands());
        output.flush();
        output.append("<<<<<<<< Конец системных команд >>>>>>>>").append("\n").flush();
    }

    public void executeExit() throws IOException, ClientDisconnectedException {
        output.append("<<<<<<<< Спасибо что используете MiniTwitter !!! >>>>>>>>").append("\n").flush();
        throw new ClientDisconnectedException();
    }

    public void executeRegister() throws IOException {

        if (securityComponent.getAuthenticationUser(userIp) != null) {
            output.append("Для того чтобы зарегистрироваться нужно выйти из системы.").append("\n").flush();
            return;
        }

        output.append("<<<<<<<<<< Регистрация нового пользователя >>>>>>>>>>").append("\n").flush();
        int intType = -1;

        while (intType != 0 && intType != 1) {

            try {
                output.append("Введите 0 если человек, 1 если организация: ").flush();
                String input = this.input.readLine();

                if (input.trim().isEmpty()) {
                    output.append("Тип пользователя не может быть пустым.").append("\n").flush();
                    continue;
                }

                intType = Integer.parseInt(input);

                if (intType != 0 && intType != 1) {
                    output.append("Введен некорректный тип пользователя.").append("\n").flush();
                }
            } catch (InputMismatchException | NumberFormatException ex) {
                output.append("Введите числовое значение.").append("\n").flush();
            }
        }

        try {
            UserType userType = UserType.getUserType(intType);

            output.append("Введите логин: ").flush();
            String login = this.input.readLine();
            login = login.trim();

            if (login.trim().isEmpty()) {
                output.append("Логин не может быть пустым.").append("\n").flush();
                return;
            }

            output.append("Введите пароль: ").flush();
            String password = this.input.readLine();
            password = password.trim();

            if (password.trim().isEmpty()) {
                output.append("Пароль не может быть пустым.").append("\n").flush();
                return;
            }

            User user;
            if (userType == UserType.getUserType(0)) {
                user = buildPerson();
            } else {
                user = buildOrganization();
            }
            user.setLogin(login);
            user.setPassword(password);
            user.setUserType(userType);


            if (userService.isUserExists(login)) {
                output.append("Пользователь под таким логином уже есть.").append("\n").flush();
                return;
            }
            output.append(userService.saveNewUser(user).beautify()).append("\n").flush();

        }catch (NullPointerException ex) {
            return;
        } catch (Exception e) {
            output.append(e.getMessage()).append("\n").flush();
        }

        output.append("<<<<<<<<<< регистрация прошла успешно! >>>>>>>>>>").append("\n").flush();
    }

    private Person buildPerson() throws IOException {
        Person person = new Person();

        output.append("Введите имя: ").flush();
        String name = this.input.readLine();
        name = name.trim();

        if (name.trim().isEmpty()) {
            output.append("Имя не может быть пустым.").append("\n").flush();
            return null;
        }

        output.append("Введите фаимилию: ").flush();
        String lastName = this.input.readLine();
        lastName = lastName.trim();

        if (lastName.trim().isEmpty()) {
            output.append("Фамилия не может быть пустой.").append("\n").flush();
            return null;
        }

        output.append("Введите дату рождения в формате(гггг-мм-дд): ").flush();
        String dateOfBirth = this.input.readLine();
        String regex = "^\\d{4}-\\d{2}-\\d{2}$";
        if (!dateOfBirth.matches(regex)) {
            output.append("Введите корректную дату.").append("\n").flush();
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate birthDate = LocalDate.parse(dateOfBirth, formatter);

        if (birthDate.isAfter(LocalDate.now()) || birthDate.isBefore(LocalDate.now().minusYears(120))) {
            output.append("Дата рождения не может быть в будущем, и должна быть в пределах 120 лет").append("\n").flush();
            return null;
        }

        person.setName(name);
        person.setLastName(lastName);
        person.setBirthDate(birthDate);

        return person;
    }

    private Organization buildOrganization() throws IOException {
        Organization organization = new Organization();
        output.append("Введите названия организации: ").flush();
        String title = this.input.readLine();
        title = title.trim();

        if (title.trim().isEmpty()) {
            output.append("Название организации не может быть пустым").append("\n").flush();
            return null;
        }

        output.append("Введите род деятельности: ").flush();
        String specialization = this.input.readLine();
        specialization = specialization.trim();

        if (specialization.trim().isEmpty()) {
            output.append("Род деятельности не может быть пустым").append("\n").flush();
            return null;
        }

        output.append("Введите дату основания: ").flush();
        String dateOfFoundation = this.input.readLine();
        String regex = "^\\d{4}-\\d{2}-\\d{2}$";
        if (!dateOfFoundation.matches(regex)) {
            output.append("Неверно введена дата основания").append("\n").flush();
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate foundationDate = LocalDate.parse(dateOfFoundation, formatter);

        if (foundationDate.isAfter(LocalDate.now()) || foundationDate.isBefore(LocalDate.now().minusYears(225))) {
            output.append("Дата основания не может быть в будущем, и старше 1800 года").append("\n").flush();
            return null;
        }

        organization.setTitle(title);
        organization.setSpecialization(specialization);
        organization.setDateOfFoundation(foundationDate);

        return organization;
    }

    public void executeLogin() throws IOException {
        output.append("<<<<<<<<<< Вход в систему >>>>>>>>>>").append("\n").flush();

        if (securityComponent.getAuthenticationUser(userIp) != null) {
            output.append("Для входа в систему, необходимо сначала разлогиниться.").append("\n").flush();
            return;
        }

        try {
            output.append("Введите логин: ").flush();
            String login = this.input.readLine();
            if (!userService.isUserExists(login)) {
                output.append("Не удалось найти пользователя с таким логином.").append("\n").flush();
                return;
            }
            output.append("Введите пароль: ").flush();
            String password = this.input.readLine();
            User user = userService.getUserByLogin(login);
            if (!user.getPassword().equals(password)) {
                output.append("Введен неверный пароль.").append("\n").flush();
                return;
            }
            securityComponent.setAuthenticationUser(userIp, user);

            output.append("Вход в систему выполнен успешно!").append("\n").flush();

            output.append("<<<<<<<<<< Добро пожаловать, ")
                    .append(securityComponent.getAuthenticationUser(userIp).userName())
                    .append("! >>>>>>>>>>").append("\n").flush();

        } catch (UserNotFoundException ex) {
            output.append(ex.getMessage()).append("\n").flush();
        }
    }

    public void executeLogout() throws IOException {
        output.append("<<<<<<<<<< Выход с аккаунта >>>>>>>>>>").append("\n").flush();

        if (securityComponent.getAuthenticationUser(userIp) == null) {
            output.append("Для того чтобы выполить выход с аккаунта, необходимо войти в систему.").append("\n").flush();
            return;
        }
        output.append("До свидания ")
                .append(securityComponent.getAuthenticationUser(userIp).userName())
                .append("!").append("\n").flush();
        securityComponent.removeAuthenticationUser(userIp);
    }

    public void executeInfo() throws IOException {
        if (securityComponent.getAuthenticationUser(userIp) == null) {
            output.append("Для вывода информации, необходимо войти в систему.").append("\n").flush();
            return;
        }

        output.append("<<<<<<<<<< Информация о пользователе >>>>>>>>>>").append("\n").flush();

        output.append(securityComponent.getAuthenticationUser(userIp).beautify()).append("\n").flush();

        output.append("<<<<<<<<<< Конец информации >>>>>>>>>>").append("\n").flush();
    }

    public void executeInfoByLogin() throws IOException {
        if (securityComponent.getAuthenticationUser(userIp) == null) {
            output.append("Для вывода информации, необходимо войти в систему.").append("\n").flush();
            return;
        }

        output.append("<<<<<<<<<< Информация о пользователе по логину. >>>>>>>>>>").append("\n").flush();

        try {
            output.append("Введите логин пользователя: ").flush();
            String login = this.input.readLine();

            login = login.trim();
            if (login.isEmpty()) {
                output.append("Логин не может быть пустым.").append("\n").flush();
                return;
            }

            User user = userService.getUserByLogin(login);

            if (user != null) {
                output.append(user.beautify()).append("\n").flush();
            } else {
                return;
            }

            output.append("<<<<<<<<<< Конец информации >>>>>>>>>>").append("\n").flush();

        } catch (UserNotFoundException ex) {
            output.append(ex.getMessage()).append("\n").flush();
        }
    }

    public void executeInfoByUserType() throws IOException {
        if (securityComponent.getAuthenticationUser(userIp) == null) {
            output.append("Для вывода информации по типу пользователя, необходимо войти в систему.").append("\n").flush();
            return;
        }

        output.append("<<<<<<<<<<  Вывод информации о всех пользователях, по типу пользователя >>>>>>>>>>").append("\n").flush();
        int intType = -1;

        while (intType != 0 && intType != 1) {
            try {
                output.append("Введите тип пользователя(0 - человек, 1 - организация): ").flush();
                String input = this.input.readLine();

                if (input.trim().isEmpty()) {
                    output.append("Тип пользователя не может быть пустым.").append("\n").flush();
                    continue;
                }

                intType = Integer.parseInt(input);

                if (intType != 0 && intType != 1) {
                    output.append("Введен некорректный тип пользователя.").append("\n").flush();
                }

            } catch (InputMismatchException e) {
                output.append("Введите числовое значение").append("\n").flush();
            }
        }

        try {
            UserType userType = UserType.getUserType(intType);

            List<User> userList = userService.getAllUsersByUserType(userType);

            for (User user : userList) {
                if (user != null) {
                    output.append(user.beautify()).append("\n").flush();
                }
            }

        } catch (UserNotFoundException | UnknownUserTypeException ex) {
            output.append(ex.getMessage()).append("\n").flush();
        }

        output.append("<<<<<<<<<< Конец информации >>>>>>>>>>").append("\n").flush();
    }

    public void executeInfoAll() throws IOException {
        if (securityComponent.getAuthenticationUser(userIp) == null) {
            output.append("Для вывода информации, необходимо войти в систему.").append("\n").flush();
            return;
        }

        output.append("<<<<<<<<<< Информация о всех пользователях системы. >>>>>>>>>>").append("\n").flush();

        try {
            List<User> users = userService.getAllUsers();
            for (User user : users) {
                if (user != null) {
                    output.append(user.beautify()).append("\n").flush();
                } else output.append("Список пользователей пуст.").append("\n").flush();
            }
        } catch (UserNotFoundException ex) {
            output.append(ex.getMessage()).append("\n").flush();
        }

        output.append("<<<<<<<<<< Конец информации >>>>>>>>>>").append("\n").flush();
    }

}

