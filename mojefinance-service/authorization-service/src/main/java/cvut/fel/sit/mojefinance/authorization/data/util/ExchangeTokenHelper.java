package cvut.fel.sit.mojefinance.authorization.data.util;

import cvut.fel.sit.mojefinance.authorization.data.exception.ClientRegistrationNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationExchange;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponse;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExchangeTokenHelper {
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> tokenResponseClient;
    private final OAuth2AuthorizedClientService authorizedClientService;

    public void exchangeToken(String clientRegistrationId, String code) throws ClientRegistrationNotFoundException {
        try {
            ClientRegistration clientRegistration = getClientRegistration(clientRegistrationId);

            OAuth2AuthorizationRequest authorizationRequest = getOAuth2AuthorizationRequest(clientRegistration);
            OAuth2AuthorizationResponse authorizationResponse = getOAuth2AuthorizationResponse(code, clientRegistration);
            OAuth2AuthorizationExchange authorizationExchange = getOAuth2AuthorizationExchange(authorizationRequest, authorizationResponse);
            OAuth2AuthorizationCodeGrantRequest tokenRequest = getOAuth2AuthorizationCodeGrantRequest(clientRegistration, authorizationExchange);

            log.debug("Exchanging authorization code for tokens.");
            var tokenResponse = tokenResponseClient.getTokenResponse(tokenRequest);
            OAuth2AccessToken accessToken = tokenResponse.getAccessToken();

            if (accessToken == null) {
                throw new RuntimeException("No access token returned from token endpoint.");
            }

            log.info("Successfully exchanged authorization code for OAuth2 tokens.");

            Authentication principal = SecurityContextHolder.getContext().getAuthentication();
            OAuth2AuthorizedClient authorizedClient = getOAuth2AuthorizedClient(clientRegistration, principal, accessToken, tokenResponse);
            authorizedClientService.saveAuthorizedClient(authorizedClient, principal);
        } catch (NullPointerException e) {
            throw new RuntimeException(e);
        }
    }

    private ClientRegistration getClientRegistration(String clientRegistrationId) throws ClientRegistrationNotFoundException {
        ClientRegistration clientRegistration = clientRegistrationRepository.findByRegistrationId(clientRegistrationId);
        if (clientRegistration == null) {
            throw new ClientRegistrationNotFoundException("Client registration not found for id: " + clientRegistrationId);
        }
        return clientRegistration;
    }

    private OAuth2AuthorizedClient getOAuth2AuthorizedClient(ClientRegistration clientRegistration, Authentication principal, OAuth2AccessToken accessToken, OAuth2AccessTokenResponse tokenResponse) {
        return new OAuth2AuthorizedClient(
                clientRegistration,
                principal.getName(),
                accessToken,
                tokenResponse.getRefreshToken()
        );
    }

    private OAuth2AuthorizationCodeGrantRequest getOAuth2AuthorizationCodeGrantRequest(ClientRegistration clientRegistration, OAuth2AuthorizationExchange authorizationExchange) {
        return new OAuth2AuthorizationCodeGrantRequest(
                clientRegistration,
                authorizationExchange
        );
    }

    private OAuth2AuthorizationExchange getOAuth2AuthorizationExchange(OAuth2AuthorizationRequest authorizationRequest, OAuth2AuthorizationResponse authorizationResponse) {
        return new OAuth2AuthorizationExchange(
                authorizationRequest,
                authorizationResponse
        );
    }

    private OAuth2AuthorizationResponse getOAuth2AuthorizationResponse(String code, ClientRegistration clientRegistration) {
        return OAuth2AuthorizationResponse
                .success(code)
                .redirectUri(clientRegistration.getRedirectUri())
                .build();
    }

    private OAuth2AuthorizationRequest getOAuth2AuthorizationRequest(ClientRegistration clientRegistration) {
        return OAuth2AuthorizationRequest
                .authorizationCode()
                .clientId(clientRegistration.getClientId())
                .authorizationUri(clientRegistration.getProviderDetails().getAuthorizationUri())
                .redirectUri(clientRegistration.getRedirectUri())
                .scopes(clientRegistration.getScopes())
                .state("state")
                .build();
    }
}

