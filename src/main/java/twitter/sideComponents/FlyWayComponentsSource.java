package twitter.sideComponents;

import org.flywaydb.core.Flyway;
import twitter.configuration.Profile;
import twitter.configuration.SideComponent;
import twitter.configuration.SideMethod;
import twitter.configuration.Value;

@SideComponent
@Profile(active = {"default", "prod"})
public class FlyWayComponentsSource {

    @Value(key = "database.url")
    private String dbUrl;

    @Value(key = "database.user")
    private String dbUser;

    @Value(key = "database.password")
    private String dbPassword;

    @SideMethod
    public Flyway flyway() {
        return Flyway
                .configure()
                .driver("org.postgresql.Driver")
                .dataSource(dbUrl, dbUser, dbPassword)
                .validateMigrationNaming(true)
                .validateOnMigrate(true)
                .baselineOnMigrate(true)
                .outOfOrder(true)
                .load();
    }

}
