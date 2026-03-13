package cvut.fel.sit.mojefinance.authorization;

import cvut.fel.sit.mojefinance.authorization.data.dto.AuthorizedClientServiceRequest;
import cvut.fel.sit.mojefinance.authorization.data.dto.ConnectAuthorizedClientRequest;
import cvut.fel.sit.mojefinance.authorization.data.exception.ClientRegistrationNotFoundException;
import cvut.fel.sit.mojefinance.authorization.data.service.AuthorizedClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthorizationServiceImpl implements AuthorizationService {
    private final AuthorizedClientService authorizedClientService;

    @Override
    public void connectAuthorizedClient(ConnectAuthorizedClientRequest request) throws ClientRegistrationNotFoundException {
        authorizedClientService.connectAuthorizedClient(request);
    }

    @Override
    public void disconnectAuthorizedClient(AuthorizedClientServiceRequest request) {
        authorizedClientService.disconnectAuthorizedClient(request);
    }

    @Override
    public boolean authorizedClientExists(AuthorizedClientServiceRequest request) {
        return authorizedClientService.authorizedClientExists(request);
    }

    @Override
    public String authorizeClient(String clientRegistrationId) {
        return authorizedClientService.authorizeClient(clientRegistrationId);
    }
}
