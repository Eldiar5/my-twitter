package twitter.sideComponents;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import twitter.configuration.Profile;
import twitter.configuration.SideComponent;
import twitter.configuration.SideMethod;
import twitter.configuration.Value;

import java.util.HashMap;
import java.util.Map;

@SideComponent
@Profile(active = {"default", "prod"})
public class HibernateEntityManagerFactory {

    @Value(key = "database.url")
    private String dbUrl;

    @Value(key = "database.user")
    private String dbUser;

    @Value(key = "database.password")
    private String dbPassword;

    @SideMethod
    public EntityManagerFactory entityManagerFactory() {
        //Database properties
        Map<String, String>  properties = new HashMap<>();
        properties.put("jakarta.persistence.jdbc.driver", "org.postgresql.Driver");
        properties.put("jakarta.persistence.jdbc.url", dbUrl);
        properties.put("jakarta.persistence.jdbc.user", dbUser);
        properties.put("jakarta.persistence.jdbc.password", dbPassword);

        return Persistence.createEntityManagerFactory("my-pu", properties);
    }

}
