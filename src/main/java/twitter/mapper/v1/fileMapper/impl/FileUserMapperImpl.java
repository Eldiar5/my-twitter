package twitter.mapper.v1.fileMapper.impl;

import twitter.configuration.Component;
import twitter.configuration.Injection;
import twitter.configuration.Profile;
import twitter.entity.user.Organization;
import twitter.entity.user.Person;
import twitter.entity.user.User;
import twitter.entity.user.UserType;
import twitter.exceptions.UnknownUserTypeException;
import twitter.mapper.v1.fileMapper.FileUserMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

@Component
@Profile(active = "test")
public class FileUserMapperImpl implements FileUserMapper {

    @Injection
    public FileUserMapperImpl() {}

    @Override
    public User buildUserFromFile(String line) throws UnknownUserTypeException {
        User user = null;

        String userType = line.substring(1, line.indexOf("}"));
        String tempVariable1 = line.substring(line.indexOf("}") + 1);
        Long userId = (long) Integer.parseInt(tempVariable1.substring(1, tempVariable1.indexOf("}")));
        String tempVariable2 = tempVariable1.substring(tempVariable1.indexOf("}") + 1);
        String userLogin = tempVariable2.substring(1, tempVariable2.indexOf("}"));
        String tempVariable3 = tempVariable2.substring(tempVariable2.indexOf("}") + 1);
        String userPass = tempVariable3.substring(1, tempVariable3.indexOf("}"));
        String tempVariable4 = tempVariable3.substring(tempVariable3.indexOf("}") + 1);

        if (userType.equals("PERSON")) {
            user = buildPersonFromFile(tempVariable4);
            user.setUserType(UserType.getUserType(0));
        } else if(userType.equals("ORGANIZATION")) {
            user = buildOrganizationFromFile(tempVariable4);
            user.setUserType(UserType.getUserType(1));
        }

        user.setId(userId);
        user.setLogin(userLogin);
        user.setPasswordHash(userPass);

        return user;
    }

    @Override
    public Person buildPersonFromFile(String line) {
        Person person = new Person();

        String userName = line.substring(1, line.indexOf("}"));
        String tempVariable1 = line.substring(line.indexOf("}") + 1);
        String lastName = tempVariable1.substring(1, tempVariable1.indexOf("}"));
        String tempVariable2 = tempVariable1.substring(tempVariable1.indexOf("}") + 1);
        String birthDateString = tempVariable2.substring(1, tempVariable2.indexOf("}"));

        LocalDate birthDate = LocalDate.parse(birthDateString);

        person.setName(userName);
        person.setLastName(lastName);
        person.setBirthDate(birthDate);

        return person;
    }

    @Override
    public Organization buildOrganizationFromFile(String line) {
        Organization organization = new Organization();

        String title = line.substring(1, line.indexOf("}"));
        String tempVariable1 = line.substring(line.indexOf("}") + 1);
        String specialization = tempVariable1.substring(1, tempVariable1.indexOf("}"));
        String tempVariable2 = tempVariable1.substring(tempVariable1.indexOf("}") + 1);
        String dateOfFoundationString = tempVariable2.substring(1, tempVariable2.indexOf("}"));

        LocalDate dateOfFoundation = LocalDate.parse(dateOfFoundationString);

        organization.setTitle(title);
        organization.setSpecialization(specialization);
        organization.setDateOfFoundation(dateOfFoundation);

        return organization;
    }

    @Override
    public User getUserFromDB(ResultSet resultSet) throws SQLException {

        String type = resultSet.getString("usertype");

        if (type.equals("PERSON")) {
            Person person = new Person();

            person.setId((long) resultSet.getInt("id"));
            person.setLogin(resultSet.getString("login"));
            person.setPasswordHash(resultSet.getString("password"));
            person.setUserType(UserType.PERSON);
            person.setRegistrationDate(resultSet.getTimestamp("registered").toLocalDateTime());
            person.setName(resultSet.getString("name"));
            person.setLastName(resultSet.getString("lastname"));
            person.setBirthDate(resultSet.getDate("date_of_birth").toLocalDate());
            return person;
        }

        if(type.equals("ORGANIZATION")) {
            Organization organization = new Organization();

            organization.setId((long) resultSet.getInt("id"));
            organization.setLogin(resultSet.getString("login"));
            organization.setPasswordHash(resultSet.getString("password"));
            organization.setUserType(UserType.ORGANIZATION);
            organization.setRegistrationDate(resultSet.getTimestamp("registered").toLocalDateTime());
            organization.setTitle(resultSet.getString("title"));
            organization.setSpecialization(resultSet.getString("specialization"));
            organization.setDateOfFoundation(resultSet.getDate("date_of_foundation").toLocalDate());
            return organization;
        }

        throw new SQLException("");
    }

}
