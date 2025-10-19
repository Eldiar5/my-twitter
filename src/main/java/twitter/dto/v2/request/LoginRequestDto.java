package twitter.dto.v2.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class LoginRequestDto {

    private String login;
    private String password;

}
