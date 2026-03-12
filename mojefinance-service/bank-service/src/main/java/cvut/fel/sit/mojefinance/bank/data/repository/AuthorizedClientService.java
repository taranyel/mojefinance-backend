package cvut.fel.sit.mojefinance.bank.data.repository;

import cvut.fel.sit.mojefinance.bank.data.dto.AuthorizedClientServiceRequest;
import cvut.fel.sit.mojefinance.bank.data.dto.ConnectAuthorizedClientRequest;
import cvut.fel.sit.mojefinance.bank.data.exception.ClientRegistrationNotFoundException;


public interface AuthorizedClientService {
    boolean authorizedClientExists(AuthorizedClientServiceRequest request);

    void connectAuthorizedClient(ConnectAuthorizedClientRequest request) throws ClientRegistrationNotFoundException;

    void disconnectAuthorizedClient(AuthorizedClientServiceRequest request);
}
