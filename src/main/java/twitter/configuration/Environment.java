package twitter.configuration;

import java.util.Map;

public class Environment {

    public final Map<String, Object> env;

    public String getApplicationProfile() {
        return (String) this.env.getOrDefault("application.profile", "default");
    }

    public Environment(Map<String, Object> env) {
        this.env = env;
    }

    public Object getProperty(String property) {
        return this.env.get(property);
    }

}
