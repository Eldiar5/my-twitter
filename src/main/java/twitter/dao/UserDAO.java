package twitter.dao;

import twitter.entity.user.User;
import twitter.entity.user.UserType;
import twitter.exceptions.UserNotFoundException;

import java.util.List;

public interface UserDAO {

    User saveNewUser(User user) throws UserNotFoundException;

    User getUserByLogin(String login) throws UserNotFoundException ;
    User getUserById(int id) throws UserNotFoundException ;
    List<User> getAllUsersByUserType(UserType userType) throws UserNotFoundException;
    List<User> getAllUsers() throws UserNotFoundException;
    boolean isUserExists(String login) throws UserNotFoundException;

}
