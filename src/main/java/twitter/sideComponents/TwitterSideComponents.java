package twitter.sideComponents;

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

}
