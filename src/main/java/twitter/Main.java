package twitter;

import org.flywaydb.core.Flyway;
import twitter.configuration.ComponentFactory;
import twitter.configuration.Environment;
import twitter.configuration.EnvironmentBuilder;
import twitter.runner.ApplicationRunner;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        Environment environment = EnvironmentBuilder
                .buildEnvironment()
                .build();

        ComponentFactory factory = new ComponentFactory(Main.class, environment);
        factory.configure();

        String activeProfile = environment.getApplicationProfile();

        if (!"test".equals(activeProfile)) {

            System.out.println("Запуск миграции базы данных для профиля: " + activeProfile);
            Flyway flyway = factory.getComponent(Flyway.class);

            if (flyway != null) {
                flyway.migrate();
            } else {
                System.err.println("ОШИБКА: не найден Flyway компонент для профиля: " + activeProfile);
            }

        }else {System.out.println("Профиль 'test' активен, пропуск миграции базы данных.");}

        ApplicationRunner runner = factory.getComponent(ApplicationRunner.class);
        runner.run();
    }
}
