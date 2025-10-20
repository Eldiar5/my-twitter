package twitter.dao.impl;

import twitter.configuration.Component;
import twitter.configuration.Injection;
import twitter.configuration.Profile;
import twitter.dao.UserDAO;
import twitter.entity.user.User;
import twitter.entity.user.UserType;
import twitter.exceptions.UnknownUserTypeException;
import twitter.exceptions.UserNotFoundException;
import twitter.mapper.fileMapper.FileUserMapper;

import java.io.*;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;


@Component
@Profile(active = "test")
public class FileUserDAO implements UserDAO {

    private final List<User> users;
    private Long id;

    private final FileUserMapper userMapper;

    @Injection
    public FileUserDAO(FileUserMapper userMapper) {
        this.userMapper = userMapper;
        users = new LinkedList<>();
        getFromFile();
    }

    public synchronized void saveToFile(User user) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("usersData.txt", true))) {
            writer.write(user.toFile());
            writer.newLine();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private synchronized void getFromFile() {
        Long maxId = 0L;

        try (BufferedReader reader = new BufferedReader(new FileReader("usersData.txt"))) {

            while (reader.ready()) {
                String line = reader.readLine();
                User userFromFile = userMapper.buildUserFromFile(line);
                users.add(userFromFile);

                if (userFromFile.getId() > maxId) {
                    maxId = userFromFile.getId();
                }
            }

        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            System.exit(1);
        } catch (UnknownUserTypeException ex) {
            System.out.println(ex.getMessage());
        }
        id = maxId;
    }

    @Override
    public synchronized User save(User user) throws UserNotFoundException {

        id = id + 1;
        user.setId(id);
        user.setRegistrationDate(LocalDateTime.now());
        users.add(user);
        saveToFile(user);

        return user;
    }

    @Override
    public synchronized User getUserByLogin(String login) throws UserNotFoundException {

        return this.users
                .stream()
                .filter(user -> user.getLogin().equals(login))
                .findFirst()
                .orElseThrow(() -> new UserNotFoundException("Пользователь под логином '" + login + "' не найден"));
    }

    @Override
    public synchronized boolean isUserExists(String login) throws UserNotFoundException {

        return users
                .stream()
                .anyMatch(user -> user.getLogin().equals(login));
    }

    @Override
    public synchronized User getUserById(int id) throws UserNotFoundException {

        return this.users
                .stream()
                .filter(user -> user.getId() == id)
                .findFirst()
                .orElseThrow(() -> new UserNotFoundException("Пользователь под ID '" + id + "' не найден"));
    }

    @Override
    public synchronized List<User> getAllUsers() throws UserNotFoundException {
        if (this.users.isEmpty()) {
            throw new UserNotFoundException("Список пользователей пуст");
        }

        return this.users;
    }

    @Override
    public synchronized List<User> getAllUsersByUserType(UserType userType) throws UserNotFoundException {
        if (this.users.isEmpty()) {
            throw new UserNotFoundException("Список пользователей пуст");
        }

        return this.users
                .stream()
                .filter(user -> user.getUserType() == userType)
                .toList();
    }
}
