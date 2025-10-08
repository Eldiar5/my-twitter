package twitter.sideComponents;

import twitter.configuration.SideComponent;
import twitter.configuration.SideMethod;

import java.util.Scanner;

@SideComponent
public class TwitterSideComponents {

    @SideMethod
    public Scanner getScanner() {
        return new Scanner(System.in);
    }

}
