package cvut.fel.sit.mojefinance.product.domain.helper;

import cvut.fel.sit.mojefinance.authorization.AuthorizationService;
import cvut.fel.sit.mojefinance.bank.domain.dto.ConnectedBanksResponse;
import cvut.fel.sit.mojefinance.bank.domain.entity.BankConnection;
import cvut.fel.sit.mojefinance.bank.domain.entity.BankConnectionStatus;
import cvut.fel.sit.mojefinance.product.domain.dto.AccountBalancesMessagingRequest;
import cvut.fel.sit.mojefinance.product.domain.entity.BankDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductHelperTest {
    @Mock
    private AuthorizationService authorizationService;

    @InjectMocks
    private ProductHelper productHelper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testBuildAccountBalancesMessagingRequest() {
        String productId = "123";
        BankDetails bankDetails = BankDetails.builder().bankName("Test Bank").clientRegistrationId("test-id").build();
        String authorization = "auth-token";
        String principalName = "test-user";

        AccountBalancesMessagingRequest request = productHelper.buildAccountBalancesMessagingRequest(productId, bankDetails, authorization, principalName);

        assertNotNull(request);
        assertEquals(productId, request.getAccountId());
        assertEquals(bankDetails, request.getBankDetails());
        assertEquals(authorization, request.getAuthorization());
        assertEquals(principalName, request.getPrincipalName());
    }

    @Test
    void testFilterRealBanks() {
        BankConnection realBank = new BankConnection();
        realBank.setManuallyCreated(false);
        BankConnection manualBank = new BankConnection();
        manualBank.setManuallyCreated(true);

        List<BankConnection> result = productHelper.filterRealBanks(List.of(realBank, manualBank));

        assertEquals(1, result.size());
        assertFalse(result.get(0).getManuallyCreated());
    }

    @Test
    void testFilterBanksWithActiveConnection() {
        BankConnection activeBank = new BankConnection();
        activeBank.setBankConnectionStatus(BankConnectionStatus.CONNECTED);
        BankConnection inactiveBank = new BankConnection();
        inactiveBank.setBankConnectionStatus(BankConnectionStatus.DISCONNECTED);

        ConnectedBanksResponse response = ConnectedBanksResponse.builder().build();
        response.setConnectedBanks(List.of(activeBank, inactiveBank));

        List<BankConnection> result = productHelper.filterBanksWithActiveConnection(response);

        assertEquals(1, result.size());
        assertEquals(BankConnectionStatus.CONNECTED, result.get(0).getBankConnectionStatus());
    }

    @Test
    void testConstructAuthorizationHeader() {
        String clientRegistrationId = "test-id";
        when(authorizationService.authorizeClient(clientRegistrationId)).thenReturn("token");

        String header = productHelper.constructAuthorizationHeader(clientRegistrationId);

        assertEquals("Bearer token", header);
        verify(authorizationService).authorizeClient(clientRegistrationId);
    }
}
