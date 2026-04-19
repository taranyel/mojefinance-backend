package cvut.fel.sit.mojefinance.authorization.data.service;

import cvut.fel.sit.mojefinance.authorization.data.dto.AuthorizedClientServiceRequest;
import cvut.fel.sit.mojefinance.authorization.data.dto.ConnectAuthorizedClientRequest;
import cvut.fel.sit.mojefinance.authorization.data.exception.ClientRegistrationNotFoundException;
import cvut.fel.sit.mojefinance.authorization.data.util.ExchangeTokenHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.stereotype.Service;

import static cvut.fel.sit.shared.util.Constants.RAIFFEISEN_BANK_CLIENT_REGISTRATION_ID;


@Slf4j
@Service
@RequiredArgsConstructor
public class AuthorizedClientServiceImpl implements AuthorizedClientService {
    private final ExchangeTokenHelper exchangeTokenHelper;
    private final OAuth2AuthorizedClientService oAuth2AuthorizedClientService;
    private final OAuth2AuthorizedClientManager authorizedClientManager;

    @Override
    public boolean authorizedClientExists(AuthorizedClientServiceRequest request) {
        return oAuth2AuthorizedClientService.loadAuthorizedClient(request.getClientRegistrationId(), request.getPrincipalName()) != null;
    }

    @Override
    public void connectAuthorizedClient(ConnectAuthorizedClientRequest request) throws ClientRegistrationNotFoundException {
        exchangeTokenHelper.exchangeToken(request.getClientRegistrationId(), request.getCode());
    }

    @Override
    public void disconnectAuthorizedClient(AuthorizedClientServiceRequest request) {
        oAuth2AuthorizedClientService.removeAuthorizedClient(request.getClientRegistrationId(), request.getPrincipalName());
    }

    @Override
    public String authorizeClient(String clientRegistrationId) {
        if (clientRegistrationId == null) {
            throw new IllegalArgumentException("Client registration ID must be provided");
        }
        if (RAIFFEISEN_BANK_CLIENT_REGISTRATION_ID.equals(clientRegistrationId)) {
            return null;
        }

        log.trace("Attempting to authorize client: {}", clientRegistrationId);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                .withClientRegistrationId(clientRegistrationId)
                .principal(authentication)
                .build();

        OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(authorizeRequest);

        if (authorizedClient != null && authorizedClient.getAccessToken() != null) {
            return authorizedClient.getAccessToken().getTokenValue();
        } else {
            throw new SecurityException("Failed to authorize client: " + clientRegistrationId);
        }
    }
}
