package cvut.fel.sit.mojefinance.bank.domain.service;

import cvut.fel.sit.mojefinance.bank.data.entity.BankConnectionId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import java.util.List;
import cvut.fel.sit.mojefinance.authorization.AuthorizationService;
import cvut.fel.sit.mojefinance.authorization.data.dto.ConnectAuthorizedClientRequest;
import cvut.fel.sit.mojefinance.authorization.data.exception.ClientRegistrationNotFoundException;
import cvut.fel.sit.mojefinance.bank.data.entity.BankConnectionEntity;
import cvut.fel.sit.mojefinance.bank.data.repository.BankConnectionRepository;
import cvut.fel.sit.mojefinance.bank.domain.dto.ConnectBankDomainRequest;
import cvut.fel.sit.mojefinance.bank.domain.dto.ConnectedBanksResponse;
import cvut.fel.sit.mojefinance.bank.domain.entity.BankConnection;
import cvut.fel.sit.mojefinance.bank.domain.entity.BankConnectionStatus;
import cvut.fel.sit.mojefinance.bank.domain.mapper.BankConnectionDomainMapper;

class BankConnectionServiceImplTest {
    @Mock
    private BankConnectionDomainMapper bankConnectionDomainMapper;
    @Mock
    private BankConnectionRepository bankConnectionRepository;
    @Mock
    private AuthorizationService authorizationService;
    @Mock
    private Authentication authentication;
    @Mock
    private SecurityContext securityContext;
    @InjectMocks
    private BankConnectionServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new BankConnectionServiceImpl(bankConnectionDomainMapper, bankConnectionRepository, authorizationService);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn("user1");
    }

    @Test
    void connectBank_successful() throws Exception {
        BankConnection bankConnection = new BankConnection();
        bankConnection.setBankName("BankA");
        bankConnection.setClientRegistrationId("regA");
        ConnectBankDomainRequest domainRequest = ConnectBankDomainRequest.builder().bankConnection(bankConnection).build();
        ConnectAuthorizedClientRequest authRequest = ConnectAuthorizedClientRequest.builder().clientRegistrationId("regA").build();
        BankConnectionEntity entity = mock(BankConnectionEntity.class);
        when(bankConnectionDomainMapper.toConnectAuthorizedClientRequest(domainRequest)).thenReturn(authRequest);
        when(bankConnectionDomainMapper.toBankConnectionEntity(any())).thenReturn(entity);
        when(entity.getId()).thenReturn(new cvut.fel.sit.mojefinance.bank.data.entity.BankConnectionId());
        service.connectBank(domainRequest);
        verify(authorizationService).connectAuthorizedClient(authRequest);
        verify(bankConnectionRepository).addConnectedBank(entity);
    }

    @Test
    void connectBank_setsManuallyCreatedIfClientRegistrationNotFound() throws Exception {
        BankConnection bankConnection = new BankConnection();
        bankConnection.setBankName("BankB");
        bankConnection.setClientRegistrationId("regB");
        ConnectBankDomainRequest domainRequest = ConnectBankDomainRequest.builder().bankConnection(bankConnection).build();
        ConnectAuthorizedClientRequest authRequest = ConnectAuthorizedClientRequest.builder().clientRegistrationId("regB").build();
        BankConnectionEntity entity = mock(BankConnectionEntity.class);
        when(bankConnectionDomainMapper.toConnectAuthorizedClientRequest(domainRequest)).thenReturn(authRequest);
        when(bankConnectionDomainMapper.toBankConnectionEntity(any())).thenReturn(entity);
        when(entity.getId()).thenReturn(new cvut.fel.sit.mojefinance.bank.data.entity.BankConnectionId());
        doThrow(ClientRegistrationNotFoundException.class).when(authorizationService).connectAuthorizedClient(authRequest);
        service.connectBank(domainRequest);
        verify(bankConnectionRepository).addConnectedBank(entity);
    }

    @Test
    void connectBank_throwsIfInvalidBankConnection() {
        ConnectBankDomainRequest domainRequest = ConnectBankDomainRequest.builder().bankConnection(null).build();
        assertThrows(IllegalArgumentException.class, () -> service.connectBank(domainRequest));
    }

    @Test
    void disconnectBank_successful() {
        doNothing().when(authorizationService).disconnectAuthorizedClient(any());
        doNothing().when(bankConnectionRepository).removeConnectedBankByClientRegistrationIdAndPrincipalName(any(), any());
        service.disconnectBank("regA");
        verify(authorizationService).disconnectAuthorizedClient(any());
        verify(bankConnectionRepository).removeConnectedBankByClientRegistrationIdAndPrincipalName(eq("regA"), eq("user1"));
    }

    @Test
    void getConnectedBanks_updatesDisconnectedBanks() {
        BankConnection connected = new BankConnection();
        connected.setClientRegistrationId("regA");
        connected.setManuallyCreated(false);
        connected.setBankConnectionStatus(BankConnectionStatus.CONNECTED);
        List<BankConnection> banks = List.of(connected);
        ConnectedBanksResponse response = ConnectedBanksResponse.builder().connectedBanks(banks).build();
        when(bankConnectionRepository.getAllConnectedBanksByPrincipalName("user1")).thenReturn(response);
        when(authorizationService.authorizedClientExists(any())).thenReturn(false);

        BankConnectionId bankConnectionId = new BankConnectionId();
        bankConnectionId.setPrincipalName("user1");
        bankConnectionId.setClientRegistrationId("regA");
        BankConnectionEntity entity = BankConnectionEntity.builder()
                .id(bankConnectionId)
                .build();
        when(bankConnectionDomainMapper.toBankConnectionEntity(any())).thenReturn(entity);
        ConnectedBanksResponse result = service.getConnectedBanks();
        verify(bankConnectionRepository).updateConnectedBank(any());
        assertEquals(BankConnectionStatus.DISCONNECTED, banks.get(0).getBankConnectionStatus());
        assertEquals(response, result);
    }

    @Test
    void getConnectedBanks_doesNotUpdateManuallyCreatedBanks() {
        BankConnection connected = new BankConnection();
        connected.setClientRegistrationId("regA");
        connected.setManuallyCreated(true);
        connected.setBankConnectionStatus(BankConnectionStatus.CONNECTED);
        List<BankConnection> banks = List.of(connected);
        ConnectedBanksResponse response = ConnectedBanksResponse.builder().connectedBanks(banks).build();
        when(bankConnectionRepository.getAllConnectedBanksByPrincipalName("user1")).thenReturn(response);
        ConnectedBanksResponse result = service.getConnectedBanks();
        verify(bankConnectionRepository, never()).updateConnectedBank(any());
        assertEquals(BankConnectionStatus.CONNECTED, banks.get(0).getBankConnectionStatus());
        assertEquals(response, result);
    }
}
