package keycloak;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.keycloak.connections.httpclient.HttpClientProvider;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;

import java.util.Map;

public class RegistrationProvider implements EventListenerProvider {
    private final KeycloakSession session;

    //change this to "http://host.docker.internal:8081" if you run your backend app on local,
    //currently setup is for docker env.
    private static final String BACKEND_URL = "http://mojefinance-backend:8081";

    public RegistrationProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public void onEvent(Event event) {
        if (event.getType() == EventType.REGISTER) {
            UserModel user = session.users().getUserById(session.getContext().getRealm(), event.getUserId());

            try {
                ObjectMapper mapper = new ObjectMapper();
                String json = mapper.writeValueAsString(Map.of(
                        "username", user.getUsername(),
                        "email", user.getEmail(),
                        "firstName", user.getFirstName(),
                        "lastName", user.getLastName()
                ));

                CloseableHttpClient client = session.getProvider(HttpClientProvider.class).getHttpClient();
                HttpPost post = new HttpPost(BACKEND_URL + "/mojefinance/api/registration");
                post.setHeader("Content-Type", "application/json");
                post.setEntity(new StringEntity(json));
                client.execute(post);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onEvent(AdminEvent event, boolean includeRepresentation) {
        // optional: handle admin-created users
    }

    @Override
    public void close() {
    }
}
