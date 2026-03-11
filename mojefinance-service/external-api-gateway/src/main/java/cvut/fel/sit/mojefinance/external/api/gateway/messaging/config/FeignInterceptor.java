package cvut.fel.sit.mojefinance.external.api.gateway.messaging.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.ClientAuthorizationRequiredException;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class FeignInterceptor implements RequestInterceptor {
    private final OAuth2AuthorizedClientManager authorizedClientManager;

    @Override
    public void apply(RequestTemplate template) {
        try {
            Authentication authentication = getAuthentication();

            if (authentication == null) {
                log.trace("No authentication available, skipping OAuth2 token injection");
                return;
            }

            String clientRegistrationId = extractClientRegistrationId(template);
            if (clientRegistrationId == null) {
                log.trace("Skipping OAuth2 token injection");
                return;
            }
            log.trace("Attempting to add OAuth2 token for client: {}", clientRegistrationId);

            OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                    .withClientRegistrationId(clientRegistrationId)
                    .principal(authentication)
                    .build();

            try {
                OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(authorizeRequest);

                if (authorizedClient != null && authorizedClient.getAccessToken() != null) {
                    String token = authorizedClient.getAccessToken().getTokenValue();
                    template.header("Authorization", "Bearer " + token);
                    log.trace("Added OAuth2 access token to request for client: {}", clientRegistrationId);
                } else {
                    log.trace("No access token available for client: {}", clientRegistrationId);
                }
            } catch (ClientAuthorizationRequiredException e) {
                log.trace("Client authorization not yet required for: {} (tokens not obtained yet)", clientRegistrationId);
            }
        } catch (Exception e) {
            log.trace("Error adding OAuth2 token to Feign request: {}", e.getMessage());
        }
    }

    private String extractClientRegistrationId(RequestTemplate template) {
        String url = template.url();

        if (url.contains("ceska-sporitelna") || url.contains("csas")) {
            return "ceska-sporitelna";
        } else if (url.contains("kb.cz") || url.contains("Czech Savings Bank")) {
            return "kb";
        } else if (url.contains("csob")) {
            return "csob";
        } else if (url.contains("airbank")) {
            return "air-bank";
        }

        return null;
    }

    private Authentication getAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken) && authentication.isAuthenticated()) {
            return authentication;
        }
        return null;
    }
}
