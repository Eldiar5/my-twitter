package twitter.security;

import twitter.entity.user.User;

public interface Security {

    User getAuthenticationUser(String userIp);

    void setAuthenticationUser(String userIp, User user);

    void removeAuthenticationUser(String userIp);

}
