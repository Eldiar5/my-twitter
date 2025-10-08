package twitter.security.impl;

import twitter.configuration.Component;
import twitter.configuration.Injection;
import twitter.security.Security;
import twitter.entity.user.User;

//@Component
public class MemorySecurityComponent implements Security {

    private User authenticatedUser;

//    @Injection
    public MemorySecurityComponent() {
    }

    @Override
    public User getAuthenticationUser(String userIp) {
        return this.authenticatedUser;
    }

    @Override
    public void setAuthenticationUser(String userIp, User user) {
        this.authenticatedUser = user;
    }

    @Override
    public void removeAuthenticationUser(String userIp) {
        this.authenticatedUser = null;
    }
}
