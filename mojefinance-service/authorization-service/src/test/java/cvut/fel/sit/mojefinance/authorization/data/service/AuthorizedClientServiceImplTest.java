package cvut.fel.sit.mojefinance.authorization.data.service;

import cvut.fel.sit.mojefinance.authorization.data.dto.AuthorizedClientServiceRequest;
import cvut.fel.sit.mojefinance.authorization.data.dto.ConnectAuthorizedClientRequest;
import cvut.fel.sit.mojefinance.authorization.data.exception.ClientRegistrationNotFoundException;
import cvut.fel.sit.mojefinance.authorization.data.util.ExchangeTokenHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;

import static cvut.fel.sit.shared.util.Constants.RAIFFEISEN_BANK_CLIENT_REGISTRATION_ID;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

class AuthorizedClientServiceImplTest {
    @Mock
    private ExchangeTokenHelper exchangeTokenHelper;
    @Mock
    private OAuth2AuthorizedClientService oAuth2AuthorizedClientService;
    @Mock
    private OAuth2AuthorizedClientManager authorizedClientManager;
    @Mock
    private Authentication authentication;
    @Mock
    private SecurityContext securityContext;
    @InjectMocks
    private AuthorizedClientServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new AuthorizedClientServiceImpl(exchangeTokenHelper, oAuth2AuthorizedClientService, authorizedClientManager);
    }

    @Test
    void authorizedClientExists_returnsTrueIfClientExists() {
        AuthorizedClientServiceRequest request = mock(AuthorizedClientServiceRequest.class);
        when(request.getClientRegistrationId()).thenReturn("test-client");
        when(request.getPrincipalName()).thenReturn("user");
        when(oAuth2AuthorizedClientService.loadAuthorizedClient("test-client", "user")).thenReturn(mock(OAuth2AuthorizedClient.class));
        assertTrue(service.authorizedClientExists(request));
    }

    @Test
    void authorizedClientExists_returnsFalseIfClientDoesNotExist() {
        AuthorizedClientServiceRequest request = mock(AuthorizedClientServiceRequest.class);
        when(request.getClientRegistrationId()).thenReturn("test-client");
        when(request.getPrincipalName()).thenReturn("user");
        when(oAuth2AuthorizedClientService.loadAuthorizedClient("test-client", "user")).thenReturn(null);
        assertFalse(service.authorizedClientExists(request));
    }

    @Test
    void connectAuthorizedClient_callsExchangeTokenHelper() throws ClientRegistrationNotFoundException {
        ConnectAuthorizedClientRequest request = mock(ConnectAuthorizedClientRequest.class);
        when(request.getClientRegistrationId()).thenReturn("test-client");
        when(request.getCode()).thenReturn("code");
        service.connectAuthorizedClient(request);
        verify(exchangeTokenHelper).exchangeToken("test-client", "code");
    }

    @Test
    void disconnectAuthorizedClient_removesAuthorizedClient() {
        AuthorizedClientServiceRequest request = mock(AuthorizedClientServiceRequest.class);
        when(request.getClientRegistrationId()).thenReturn("test-client");
        when(request.getPrincipalName()).thenReturn("user");
        service.disconnectAuthorizedClient(request);
        verify(oAuth2AuthorizedClientService).removeAuthorizedClient("test-client", "user");
    }

    @Test
    void authorizeClient_throwsExceptionIfClientRegistrationIdIsNull() {
        assertThrows(IllegalArgumentException.class, () -> service.authorizeClient(null));
    }

    @Test
    void authorizeClient_returnsNullForRaiffeisenBank() {
        String result = service.authorizeClient(RAIFFEISEN_BANK_CLIENT_REGISTRATION_ID);
        assertNull(result);
    }

    @Test
    void authorizeClient_returnsAccessTokenIfAuthorized() {
        String clientRegistrationId = "test-client";
        String tokenValue = "access-token";
        OAuth2AuthorizedClient authorizedClient = mock(OAuth2AuthorizedClient.class);
        OAuth2AccessToken accessToken = mock(OAuth2AccessToken.class);
        when(accessToken.getTokenValue()).thenReturn(tokenValue);
        when(authorizedClient.getAccessToken()).thenReturn(accessToken);
        when(authorizedClientManager.authorize(any(OAuth2AuthorizeRequest.class))).thenReturn(authorizedClient);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        String result = service.authorizeClient(clientRegistrationId);
        assertEquals(tokenValue, result);
    }

    @Test
    void authorizeClient_throwsSecurityExceptionIfAuthorizationFails() {
        String clientRegistrationId = "test-client";
        when(authorizedClientManager.authorize(any(OAuth2AuthorizeRequest.class))).thenReturn(null);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        assertThrows(SecurityException.class, () -> service.authorizeClient(clientRegistrationId));
    }
}
