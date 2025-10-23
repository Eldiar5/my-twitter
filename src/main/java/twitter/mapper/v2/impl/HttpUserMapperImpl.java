package twitter.mapper.v2.impl;

import twitter.configuration.Component;
import twitter.dto.v2.response.InfoResponseDto;
import twitter.entity.user.Organization;
import twitter.entity.user.Person;
import twitter.entity.user.User;
import twitter.entity.user.UserType;
import twitter.mapper.v2.HttpUserMapper;

import java.time.format.DateTimeFormatter;

@Component
public class HttpUserMapperImpl implements HttpUserMapper {

    @Override
    public InfoResponseDto mapUserToResponseDto(User user) {
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
    }

}
