package twitter.mapper.v1.fileMapper;

import twitter.entity.user.Organization;
import twitter.entity.user.Person;
import twitter.entity.user.User;
import twitter.exceptions.UnknownUserTypeException;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface FileUserMapper {

    User buildUserFromFile(String line) throws UnknownUserTypeException;

    Person buildPersonFromFile(String line);

    Organization buildOrganizationFromFile(String line);

    User getUserFromDB(ResultSet resultSet) throws SQLException;

}
