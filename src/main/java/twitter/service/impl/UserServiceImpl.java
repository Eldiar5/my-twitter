package twitter.service.impl;

import twitter.configuration.Component;
import twitter.configuration.Injection;
import twitter.dao.UserDAO;
import twitter.entity.user.User;
import twitter.entity.user.UserType;
import twitter.exceptions.UserNotFoundException;
import twitter.service.UserService;

import java.util.List;

@Component
public class UserServiceImpl implements UserService {

    private final UserDAO userDAO;

    @Injection
    public UserServiceImpl(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public User saveNewUser(User user) throws UserNotFoundException {
        return userDAO.saveNewUser(user);
    }

    public boolean isUserExists(String login) throws UserNotFoundException {
        return userDAO.isUserExists(login);
    }

    public User getUserByLogin(String login) throws UserNotFoundException {
        return userDAO.getUserByLogin(login);
    }

    public User getUserById(int id) throws UserNotFoundException {
        return userDAO.getUserById(id);
    }

    public List<User> getAllUsers() throws UserNotFoundException {
        List<User> allUsers = userDAO.getAllUsers();

        if (allUsers.isEmpty()) {
            throw new UserNotFoundException();
        }

        return allUsers;
    }

    public List<User> getAllUsersByUserType(UserType userType) throws UserNotFoundException {
        return userDAO.getAllUsersByUserType(userType);
    }

}
