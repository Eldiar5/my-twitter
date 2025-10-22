package twitter.sideComponents;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import twitter.configuration.SideComponent;
import twitter.configuration.SideMethod;
import twitter.sideComponents.web.TwitterAllowedEndpoints;

import java.util.Scanner;

@SideComponent
public class TwitterSideComponents {

    @SideMethod
    public Scanner getScanner() {
        return new Scanner(System.in);
    }

    @SideMethod
    public TwitterAllowedEndpoints allAllowedEndpoints() {
        TwitterAllowedEndpoints allowedEndpoints = new TwitterAllowedEndpoints();

        allowedEndpoints.addAllowedEndpoint("/api/login");
        allowedEndpoints.addAllowedEndpoint("/api/register");

        return allowedEndpoints;
    }

    @SideMethod
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder(12);
    }
}
