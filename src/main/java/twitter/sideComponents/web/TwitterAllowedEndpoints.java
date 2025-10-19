package twitter.sideComponents.web;

import java.util.ArrayList;
import java.util.List;

public class TwitterAllowedEndpoints {

    private final List<String> allowedEndpoints;

    public TwitterAllowedEndpoints() {
        allowedEndpoints = new ArrayList<>();
    }

    public void addAllowedEndpoint(String endpoint) {
        allowedEndpoints.add(endpoint);
    }

    public boolean isEndpointAllowed(String endpoint) {
        return allowedEndpoints.contains(endpoint);
    }
}
