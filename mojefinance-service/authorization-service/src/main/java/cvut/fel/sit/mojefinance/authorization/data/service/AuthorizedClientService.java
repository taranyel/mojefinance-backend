package cvut.fel.sit.mojefinance.authorization.data.service;


import cvut.fel.sit.mojefinance.authorization.data.dto.AuthorizedClientServiceRequest;
import cvut.fel.sit.mojefinance.authorization.data.dto.ConnectAuthorizedClientRequest;
import cvut.fel.sit.mojefinance.authorization.data.exception.ClientRegistrationNotFoundException;

public interface AuthorizedClientService {
    boolean authorizedClientExists(AuthorizedClientServiceRequest request);

    void connectAuthorizedClient(ConnectAuthorizedClientRequest request) throws ClientRegistrationNotFoundException;

    void disconnectAuthorizedClient(AuthorizedClientServiceRequest request);

    String authorizeClient(String clientRegistrationId);
}
