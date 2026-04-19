package cvut.fel.sit.mojefinance.authorization.data.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.util.Set;

class ExchangeTokenHelperTest {
    @Mock
    private ClientRegistrationRepository clientRegistrationRepository;
    @Mock
    private OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> tokenResponseClient;
    @Mock
    private OAuth2AuthorizedClientService authorizedClientService;
    @Mock
    private Authentication authentication;
    @Mock
    private SecurityContext securityContext;
    @InjectMocks
    private ExchangeTokenHelper exchangeTokenHelper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        exchangeTokenHelper = new ExchangeTokenHelper(clientRegistrationRepository, tokenResponseClient, authorizedClientService);
    }

    @Test
    void exchangeToken_successful() throws Exception {
        String clientRegistrationId = "test-client";
        String code = "auth-code";
        ClientRegistration clientRegistration = mock(ClientRegistration.class);
        when(clientRegistrationRepository.findByRegistrationId(clientRegistrationId)).thenReturn(clientRegistration);
        when(clientRegistration.getRedirectUri()).thenReturn("http://localhost/redirect");
        when(clientRegistration.getClientId()).thenReturn("client-id");
        when(clientRegistration.getProviderDetails()).thenReturn(mock(ClientRegistration.ProviderDetails.class));
        when(clientRegistration.getProviderDetails().getAuthorizationUri()).thenReturn("http://auth-uri");
        when(clientRegistration.getScopes()).thenReturn(Set.of("scope1"));
        OAuth2AccessTokenResponse tokenResponse = mock(OAuth2AccessTokenResponse.class);
        OAuth2AccessToken accessToken = mock(OAuth2AccessToken.class);
        when(tokenResponse.getAccessToken()).thenReturn(accessToken);
        when(tokenResponseClient.getTokenResponse(any(OAuth2AuthorizationCodeGrantRequest.class))).thenReturn(tokenResponse);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("user");
        SecurityContextHolder.setContext(securityContext);
        exchangeTokenHelper.exchangeToken(clientRegistrationId, code);
        verify(authorizedClientService).saveAuthorizedClient(any(OAuth2AuthorizedClient.class), eq(authentication));
    }

    @Test
    void exchangeToken_throwsClientRegistrationNotFoundException() {
        String clientRegistrationId = "not-found";
        when(clientRegistrationRepository.findByRegistrationId(clientRegistrationId)).thenReturn(null);
        assertThrows(cvut.fel.sit.mojefinance.authorization.data.exception.ClientRegistrationNotFoundException.class, () ->
                exchangeTokenHelper.exchangeToken(clientRegistrationId, "code"));
    }

    @Test
    void exchangeToken_throwsSecurityExceptionIfNoAccessToken() {
        String clientRegistrationId = "test-client";
        String code = "auth-code";
        ClientRegistration clientRegistration = mock(ClientRegistration.class);
        when(clientRegistrationRepository.findByRegistrationId(clientRegistrationId)).thenReturn(clientRegistration);
        when(clientRegistration.getRedirectUri()).thenReturn("http://localhost/redirect");
        when(clientRegistration.getClientId()).thenReturn("client-id");
        when(clientRegistration.getProviderDetails()).thenReturn(mock(ClientRegistration.ProviderDetails.class));
        when(clientRegistration.getProviderDetails().getAuthorizationUri()).thenReturn("http://auth-uri");
        when(clientRegistration.getScopes()).thenReturn(Set.of("scope1"));
        OAuth2AccessTokenResponse tokenResponse = mock(OAuth2AccessTokenResponse.class);
        when(tokenResponse.getAccessToken()).thenReturn(null);
        when(tokenResponseClient.getTokenResponse(any(OAuth2AuthorizationCodeGrantRequest.class))).thenReturn(tokenResponse);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        assertThrows(SecurityException.class, () -> exchangeTokenHelper.exchangeToken(clientRegistrationId, code));
    }

    @Test
    void exchangeToken_throwsServiceExceptionOnNullPointer() {
        String clientRegistrationId = "test-client";
        String code = "auth-code";
        ClientRegistration clientRegistration = mock(ClientRegistration.class);
        when(clientRegistrationRepository.findByRegistrationId(clientRegistrationId)).thenReturn(clientRegistration);
        when(clientRegistration.getRedirectUri()).thenThrow(new NullPointerException("test NPE"));
        assertThrows(cvut.fel.sit.shared.exception.ServiceException.class, () -> exchangeTokenHelper.exchangeToken(clientRegistrationId, code));
    }
}
