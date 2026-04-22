package cvut.fel.sit.mojefinance.product.messaging.service;

import cvut.fel.sit.mojefinance.product.domain.dto.AccountBalancesMessagingRequest;
import cvut.fel.sit.mojefinance.product.domain.dto.ProductsResponse;
import cvut.fel.sit.mojefinance.product.domain.dto.TransactionsRequest;
import cvut.fel.sit.mojefinance.product.domain.entity.Amount;
import cvut.fel.sit.mojefinance.product.domain.entity.BankDetails;
import cvut.fel.sit.mojefinance.product.messaging.client.RaiffeisenBankApiFeignClient;
import cvut.fel.sit.mojefinance.product.messaging.dto.ProductsMessagingRequest;
import cvut.fel.sit.mojefinance.product.messaging.dto.TransactionsMessagingResponse;
import cvut.fel.sit.mojefinance.product.messaging.mapper.RaiffeisenBankApiMapper;
import cvut.fel.sit.reif.openapi.model.GetAccounts200Response;
import cvut.fel.sit.reif.openapi.model.GetBalance200Response;
import cvut.fel.sit.reif.openapi.model.GetTransactionList200Response;
import cvut.fel.sit.shared.exception.ServiceException;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static cvut.fel.sit.shared.util.Constants.CZK_CURRENCY_CODE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RaiffeisenBankApiProviderTest {

    @Mock
    private RaiffeisenBankApiFeignClient raiffeisenBankApiFeignClient;

    @Mock
    private RaiffeisenBankApiMapper raiffeisenBankApiMapper;

    @InjectMocks
    private RaiffeisenBankApiProvider raiffeisenBankApiProvider;

    private static final String CLIENT_ID = "test-ibm-client-id";
    private static final String ACCOUNT_ID = "rb-acc-123";

    @BeforeEach
    void setUp() {
        // Inject the @Value annotated field manually for testing
        ReflectionTestUtils.setField(raiffeisenBankApiProvider, "raiffeisenBankXIbmClientId", CLIENT_ID);
    }

    // --- fetchProducts Tests ---

    @Test
    void fetchProducts_ShouldReturnProductsResponse_WhenApiCallIsSuccessful() {
        // Given
        BankDetails bankDetails = BankDetails.builder().bankName("Raiffeisen Bank").build();
        ProductsMessagingRequest request = ProductsMessagingRequest.builder()
                .bankDetails(bankDetails)
                .build();

        GetAccounts200Response mockRes = new GetAccounts200Response();
        when(raiffeisenBankApiFeignClient.getAccounts(eq(CLIENT_ID), anyString()))
                .thenReturn(ResponseEntity.ok(mockRes));

        ProductsResponse expectedResponse = ProductsResponse.builder().build();
        when(raiffeisenBankApiMapper.toProductsResponse(mockRes, bankDetails)).thenReturn(expectedResponse);

        // When
        ProductsResponse actualResponse = raiffeisenBankApiProvider.fetchProducts(request);

        // Then
        assertNotNull(actualResponse);
        assertEquals(expectedResponse, actualResponse);
        verify(raiffeisenBankApiMapper).toProductsResponse(mockRes, bankDetails);
    }

    @Test
    void fetchProducts_ShouldThrowServiceException_WhenResponseHasNoBody() {
        // Given
        ProductsMessagingRequest request = ProductsMessagingRequest.builder()
                .bankDetails(BankDetails.builder().build())
                .build();

        when(raiffeisenBankApiFeignClient.getAccounts(eq(CLIENT_ID), anyString()))
                .thenReturn(ResponseEntity.ok().build()); // 200 OK but null body

        // When & Then
        ServiceException exception = assertThrows(ServiceException.class, () -> raiffeisenBankApiProvider.fetchProducts(request));
        assertTrue(exception.getMessage().contains("Failed to fetch products from Raiffeisen Bank API"));
        verifyNoInteractions(raiffeisenBankApiMapper);
    }

    @Test
    void fetchProducts_ShouldThrowServiceException_WhenFeignExceptionIsThrown() {
        // Given
        ProductsMessagingRequest request = ProductsMessagingRequest.builder().build();
        FeignException mockFeignException = mock(FeignException.class);
        when(mockFeignException.getMessage()).thenReturn("Bad Request");

        when(raiffeisenBankApiFeignClient.getAccounts(any(), any())).thenThrow(mockFeignException);

        // When & Then
        ServiceException exception = assertThrows(ServiceException.class, () -> raiffeisenBankApiProvider.fetchProducts(request));
        assertTrue(exception.getMessage().contains("Error calling Raiffeisen Bank API: Bad Request"));
    }

    // --- fetchTransactions Tests ---

    @Test
    void fetchTransactions_ShouldReturnTransactionsResponse_WhenApiCallIsSuccessful() {
        // Given
        TransactionsRequest request = TransactionsRequest.builder()
                .accountId(ACCOUNT_ID)
                .build();
        String fromDate = "2026-04-01";
        String toDate = "2026-04-30";

        GetTransactionList200Response mockRes = new GetTransactionList200Response();
        when(raiffeisenBankApiFeignClient.getTransactions(eq(CLIENT_ID), anyString(), eq(ACCOUNT_ID), eq(CZK_CURRENCY_CODE), eq(fromDate), eq(toDate)))
                .thenReturn(ResponseEntity.ok(mockRes));

        TransactionsMessagingResponse expectedResponse = TransactionsMessagingResponse.builder().build();
        when(raiffeisenBankApiMapper.toTransactionsResponse(mockRes)).thenReturn(expectedResponse);

        // When
        TransactionsMessagingResponse actualResponse = raiffeisenBankApiProvider.fetchTransactions(request, fromDate, toDate);

        // Then
        assertNotNull(actualResponse);
        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void fetchTransactions_ShouldThrowServiceException_WhenFeignExceptionIsThrown() {
        // Given
        TransactionsRequest request = TransactionsRequest.builder().accountId(ACCOUNT_ID).build();
        when(raiffeisenBankApiFeignClient.getTransactions(any(), any(), any(), any(), any(), any()))
                .thenThrow(mock(FeignException.class));

        // When & Then
        assertThrows(ServiceException.class, () -> raiffeisenBankApiProvider.fetchTransactions(request, "2026-04-01", "2026-04-30"));
    }

    // --- fetchAccountBalance Tests ---

    @Test
    void fetchAccountBalance_ShouldReturnAmount_WhenApiCallIsSuccessful() {
        // Given
        AccountBalancesMessagingRequest request = AccountBalancesMessagingRequest.builder()
                .accountId(ACCOUNT_ID)
                .build();

        GetBalance200Response mockRes = new GetBalance200Response();
        when(raiffeisenBankApiFeignClient.getAccountBalance(eq(CLIENT_ID), anyString(), eq(ACCOUNT_ID)))
                .thenReturn(ResponseEntity.ok(mockRes));

        Amount expectedAmount = Amount.builder().value(BigDecimal.valueOf(2000)).currency("CZK").build();
        when(raiffeisenBankApiMapper.toDomainBalance(mockRes)).thenReturn(expectedAmount);

        // When
        Amount actualAmount = raiffeisenBankApiProvider.fetchAccountBalance(request);

        // Then
        assertNotNull(actualAmount);
        assertEquals(expectedAmount, actualAmount);
    }

    @Test
    void fetchAccountBalance_ShouldThrowServiceException_WhenResponseIsNotSuccessful() {
        // Given
        AccountBalancesMessagingRequest request = AccountBalancesMessagingRequest.builder()
                .accountId(ACCOUNT_ID)
                .build();

        // Simulate a 404 Not Found error returned as a ResponseEntity rather than an exception
        when(raiffeisenBankApiFeignClient.getAccountBalance(any(), any(), any()))
                .thenReturn(ResponseEntity.notFound().build());

        // When & Then
        assertThrows(ServiceException.class, () -> raiffeisenBankApiProvider.fetchAccountBalance(request));
    }
}