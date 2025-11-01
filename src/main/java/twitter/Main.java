package twitter;

import org.flywaydb.core.Flyway;
import twitter.configuration.ComponentFactory;
import twitter.configuration.Environment;
import twitter.configuration.EnvironmentBuilder;
import twitter.runner.ApplicationRunner;

public class Main {
    public static void main(String[] args) {
        Environment environment = EnvironmentBuilder
                .buildEnvironment()
                .build();

        ComponentFactory.use(Main.class, environment);
        ComponentFactory.configure();

        String activeProfile = environment.getApplicationProfile();

        if (!"test".equals(activeProfile)) {

            System.out.println("Запуск миграции базы данных для профиля: " + activeProfile);
            Flyway flyway = ComponentFactory.getComponent(Flyway.class);

            if (flyway != null) {
                flyway.migrate();
            } else {
                System.err.println("ОШИБКА: не найден Flyway компонент для профиля: " + activeProfile);
            }

        }else {System.out.println("Профиль 'test' активен, пропуск миграции базы данных.");}

        ApplicationRunner runner = ComponentFactory.getComponent(ApplicationRunner.class);
        runner.run();
    }
}
