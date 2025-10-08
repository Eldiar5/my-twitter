package twitter.configuration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class EnvironmentBuilder {

    private final String defaultPropertiesFile = "application.properties";

    private final Environment environment;

    private EnvironmentBuilder() {
        this.environment = new Environment(this.initProperties(defaultPropertiesFile));
    }

    public static EnvironmentBuilder buildEnvironment() {
        return new EnvironmentBuilder();
    }

    public Environment build() {
        return this.environment;
    }

    private Map<String, Object> initProperties(String propertiesFile) {
        Map<String, Object> result = new HashMap<>();
        try (
                InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(propertiesFile);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))
        ) {
            bufferedReader.lines().forEach(line -> {
                if (Objects.nonNull(line) && !line.isBlank() && !line.startsWith("#")) {
                    String key = line.substring(0, line.indexOf("="));
                    String value = line.substring(line.indexOf("=") + 1);
                    result.put(key, value);
                }
            });
        } catch (IOException ex) {
            System.err.println("Не удалось загрузить свойства приложения. Файл: " + propertiesFile + ". " + ex.getMessage());
            System.exit(1);
        }
        return result;
    }

}
