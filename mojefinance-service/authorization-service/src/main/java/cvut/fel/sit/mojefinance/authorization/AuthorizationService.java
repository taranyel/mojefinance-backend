package cvut.fel.sit.mojefinance.authorization;

import cvut.fel.sit.mojefinance.authorization.data.dto.AuthorizedClientServiceRequest;
import cvut.fel.sit.mojefinance.authorization.data.dto.ConnectAuthorizedClientRequest;
import cvut.fel.sit.mojefinance.authorization.data.exception.ClientRegistrationNotFoundException;

public interface AuthorizationService {
    void connectAuthorizedClient(ConnectAuthorizedClientRequest request) throws ClientRegistrationNotFoundException;

    void disconnectAuthorizedClient(AuthorizedClientServiceRequest request);

    boolean authorizedClientExists(AuthorizedClientServiceRequest request);

    String authorizeClient(String clientRegistrationId);
}
