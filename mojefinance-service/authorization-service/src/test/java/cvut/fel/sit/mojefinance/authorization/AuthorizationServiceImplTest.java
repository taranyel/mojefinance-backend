package cvut.fel.sit.mojefinance.authorization;

import cvut.fel.sit.mojefinance.authorization.data.dto.AuthorizedClientServiceRequest;
import cvut.fel.sit.mojefinance.authorization.data.dto.ConnectAuthorizedClientRequest;
import cvut.fel.sit.mojefinance.authorization.data.exception.ClientRegistrationNotFoundException;
import cvut.fel.sit.mojefinance.authorization.data.service.AuthorizedClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

class AuthorizationServiceImplTest {
    @Mock
    private AuthorizedClientService authorizedClientService;
    @InjectMocks
    private AuthorizationServiceImpl authorizationServiceImpl;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authorizationServiceImpl = new AuthorizationServiceImpl(authorizedClientService);
    }

    @Test
    void connectAuthorizedClient_delegatesToService() throws ClientRegistrationNotFoundException {
        ConnectAuthorizedClientRequest request = mock(ConnectAuthorizedClientRequest.class);
        authorizationServiceImpl.connectAuthorizedClient(request);
        verify(authorizedClientService).connectAuthorizedClient(request);
    }

    @Test
    void disconnectAuthorizedClient_delegatesToService() {
        AuthorizedClientServiceRequest request = mock(AuthorizedClientServiceRequest.class);
        authorizationServiceImpl.disconnectAuthorizedClient(request);
        verify(authorizedClientService).disconnectAuthorizedClient(request);
    }

    @Test
    void authorizedClientExists_delegatesToService() {
        AuthorizedClientServiceRequest request = mock(AuthorizedClientServiceRequest.class);
        when(authorizedClientService.authorizedClientExists(request)).thenReturn(true);
        assertTrue(authorizationServiceImpl.authorizedClientExists(request));
        when(authorizedClientService.authorizedClientExists(request)).thenReturn(false);
        assertFalse(authorizationServiceImpl.authorizedClientExists(request));
    }

    @Test
    void authorizeClient_delegatesToService() {
        String clientRegistrationId = "test-client";
        when(authorizedClientService.authorizeClient(clientRegistrationId)).thenReturn("token");
        assertEquals("token", authorizationServiceImpl.authorizeClient(clientRegistrationId));
    }
}
