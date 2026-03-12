package cvut.fel.sit.mojefinance.bank.data.service;

import cvut.fel.sit.mojefinance.bank.data.dto.AuthorizedClientServiceRequest;
import cvut.fel.sit.mojefinance.bank.data.dto.ConnectAuthorizedClientRequest;
import cvut.fel.sit.mojefinance.bank.data.exception.ClientRegistrationNotFoundException;
import cvut.fel.sit.mojefinance.bank.data.repository.AuthorizedClientService;
import cvut.fel.sit.mojefinance.bank.data.util.ExchangeTokenHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AuthorizedClientServiceImpl implements AuthorizedClientService {
    private final ExchangeTokenHelper exchangeTokenHelper;
    private final OAuth2AuthorizedClientService authorizedClientService;

    @Override
    public boolean authorizedClientExists(AuthorizedClientServiceRequest request) {
        return authorizedClientService.loadAuthorizedClient(request.getClientRegistrationId(), request.getPrincipalName()) != null;
    }

    @Override
    public void connectAuthorizedClient(ConnectAuthorizedClientRequest request) throws ClientRegistrationNotFoundException {
        exchangeTokenHelper.exchangeToken(request.getClientRegistrationId(), request.getCode());
    }

    @Override
    public void disconnectAuthorizedClient(AuthorizedClientServiceRequest request) {
        authorizedClientService.removeAuthorizedClient(request.getClientRegistrationId(), request.getPrincipalName());
    }
}
