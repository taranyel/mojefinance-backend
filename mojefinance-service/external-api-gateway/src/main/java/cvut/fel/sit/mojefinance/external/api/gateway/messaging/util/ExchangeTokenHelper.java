package cvut.fel.sit.mojefinance.external.api.gateway.messaging.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationExchange;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponse;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExchangeTokenHelper {
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> tokenResponseClient;
    private final OAuth2AuthorizedClientService authorizedClientService;

    public void exchangeToken(String clientRegistrationId, String code) {
        try {
            ClientRegistration clientRegistration = clientRegistrationRepository.findByRegistrationId(clientRegistrationId);
            OAuth2AuthorizationRequest authorizationRequest = OAuth2AuthorizationRequest
                    .authorizationCode()
                    .clientId(clientRegistration.getClientId())
                    .authorizationUri(clientRegistration.getProviderDetails().getAuthorizationUri())
                    .redirectUri(clientRegistration.getRedirectUri())
                    .scopes(clientRegistration.getScopes())
                    .state("state")
                    .build();

            OAuth2AuthorizationResponse authorizationResponse = OAuth2AuthorizationResponse
                    .success(code)
                    .redirectUri(clientRegistration.getRedirectUri())
                    .build();

            OAuth2AuthorizationExchange authorizationExchange = new OAuth2AuthorizationExchange(
                    authorizationRequest,
                    authorizationResponse
            );

            OAuth2AuthorizationCodeGrantRequest tokenRequest = new OAuth2AuthorizationCodeGrantRequest(
                    clientRegistration,
                    authorizationExchange
            );

            log.debug("Exchanging authorization code for tokens.");
            var tokenResponse = tokenResponseClient.getTokenResponse(tokenRequest);
            OAuth2AccessToken accessToken = tokenResponse.getAccessToken();

            if (accessToken == null) {
                throw new RuntimeException("No access token returned from token endpoint");
            }

            log.info("Successfully exchanged authorization code for OAuth2 tokens");
            log.debug("Access Token expires at: {}", accessToken.getExpiresAt());

            Authentication principal = getOrCreateAuthentication();
            OAuth2AuthorizedClient authorizedClient = new OAuth2AuthorizedClient(
                    clientRegistration,
                    principal.getName(),
                    accessToken,
                    tokenResponse.getRefreshToken()
            );

            authorizedClientService.saveAuthorizedClient(authorizedClient, principal);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
    private Authentication getOrCreateAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            log.debug("Using authenticated principal: {}", authentication.getName());
            return authentication;
        }

        log.debug("Creating anonymous authentication for token storage");
        return new AnonymousAuthenticationToken(
                "key",
                "anonymous",
                getAuthorities()
        );
    }

    private Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }
}
