package cvut.fel.sit.mojefinance.product.messaging.service;

import cvut.fel.sit.csob.accounts.openapi.model.GetAccountsRes;
import cvut.fel.sit.csob.balances.openapi.model.GetAccountBalanceRes;
import cvut.fel.sit.csob.transactions.openapi.model.GetTransactionHistoryRes;
import cvut.fel.sit.mojefinance.product.domain.dto.AccountBalancesMessagingRequest;
import cvut.fel.sit.mojefinance.product.domain.dto.ProductsResponse;
import cvut.fel.sit.mojefinance.product.domain.dto.TransactionsRequest;
import cvut.fel.sit.mojefinance.product.domain.entity.Amount;
import cvut.fel.sit.mojefinance.product.domain.entity.BankDetails;
import cvut.fel.sit.mojefinance.product.messaging.client.CSOBApiFeignClient;
import cvut.fel.sit.mojefinance.product.messaging.dto.ProductsMessagingRequest;
import cvut.fel.sit.mojefinance.product.messaging.dto.TransactionsMessagingResponse;
import cvut.fel.sit.mojefinance.product.messaging.mapper.CSOBApiMapper;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CSOBApiProviderTest {

    @Mock
    private CSOBApiFeignClient csobApiFeignClient;

    @Mock
    private CSOBApiMapper csobApiMapper;

    @InjectMocks
    private CSOBApiProvider csobApiProvider;

    private static final String API_KEY = "test-api-key";
    private static final String TPP_NAME = "test-tpp-name";
    private static final String AUTH_TOKEN = "Bearer token";
    private static final String ACCOUNT_ID = "acc-123";
    private static final String JSON_CONTENT_TYPE = "application/json";

    @BeforeEach
    void setUp() {
        // Inject values normally provided by Spring's @Value
        ReflectionTestUtils.setField(csobApiProvider, "csobApiKey", API_KEY);
        ReflectionTestUtils.setField(csobApiProvider, "csobTppName", TPP_NAME);
    }

    // --- fetchProducts Tests ---

    @Test
    void fetchProducts_ShouldReturnProductsResponse_WhenApiCallIsSuccessful() {
        // Given
        BankDetails bankDetails = BankDetails.builder().bankName("CSOB").build();
        ProductsMessagingRequest request = ProductsMessagingRequest.builder()
                .authorization(AUTH_TOKEN)
                .bankDetails(bankDetails)
                .build();

        GetAccountsRes mockRes = new GetAccountsRes();
        when(csobApiFeignClient.getAccounts(eq(AUTH_TOKEN), anyString(), eq(true), eq(TPP_NAME), eq(API_KEY), eq(JSON_CONTENT_TYPE)))
                .thenReturn(ResponseEntity.ok(mockRes));

        ProductsResponse expectedResponse = ProductsResponse.builder().build();
        when(csobApiMapper.toProductsResponse(mockRes, bankDetails)).thenReturn(expectedResponse);

        // When
        ProductsResponse actualResponse = csobApiProvider.fetchProducts(request);

        // Then
        assertNotNull(actualResponse);
        assertEquals(expectedResponse, actualResponse);
        verify(csobApiMapper).toProductsResponse(mockRes, bankDetails);
    }

    @Test
    void fetchProducts_ShouldThrowServiceException_WhenResponseHasNoBody() {
        // Given
        ProductsMessagingRequest request = ProductsMessagingRequest.builder()
                .authorization(AUTH_TOKEN)
                .build();

        when(csobApiFeignClient.getAccounts(anyString(), anyString(), eq(true), anyString(), anyString(), anyString()))
                .thenReturn(ResponseEntity.ok().build()); // 200 OK, but NO body

        // When & Then
        ServiceException exception = assertThrows(ServiceException.class, () -> csobApiProvider.fetchProducts(request));
        assertTrue(exception.getMessage().contains("Failed to fetch products from CSOB API"));
        verifyNoInteractions(csobApiMapper);
    }

    @Test
    void fetchProducts_ShouldThrowServiceException_WhenFeignExceptionIsThrown() {
        // Given
        ProductsMessagingRequest request = ProductsMessagingRequest.builder().authorization(AUTH_TOKEN).build();
        FeignException mockFeignException = mock(FeignException.class);
        when(mockFeignException.getMessage()).thenReturn("Unauthorized");

        when(csobApiFeignClient.getAccounts(any(), any(), anyBoolean(), any(), any(), any()))
                .thenThrow(mockFeignException);

        // When & Then
        ServiceException exception = assertThrows(ServiceException.class, () -> csobApiProvider.fetchProducts(request));
        assertTrue(exception.getMessage().contains("Error calling CSOB API"));
    }

    // --- fetchTransactions Tests ---

    @Test
    void fetchTransactions_ShouldReturnTransactionsResponse_WhenApiCallIsSuccessful() {
        // Given
        TransactionsRequest request = TransactionsRequest.builder()
                .authorization(AUTH_TOKEN)
                .accountId(ACCOUNT_ID)
                .build();
        String fromDate = "2026-01-01T00:00:00Z";
        String toDate = "2026-01-31T23:59:59Z";

        GetTransactionHistoryRes mockRes = new GetTransactionHistoryRes();
        when(csobApiFeignClient.getTransactions(eq(AUTH_TOKEN), anyString(), eq(true), eq(TPP_NAME), eq(API_KEY), eq(JSON_CONTENT_TYPE), eq(ACCOUNT_ID), eq(fromDate), eq(toDate)))
                .thenReturn(ResponseEntity.ok(mockRes));

        TransactionsMessagingResponse expectedResponse = TransactionsMessagingResponse.builder().build();
        when(csobApiMapper.toTransactionsResponse(mockRes)).thenReturn(expectedResponse);

        // When
        TransactionsMessagingResponse actualResponse = csobApiProvider.fetchTransactions(request, fromDate, toDate);

        // Then
        assertNotNull(actualResponse);
        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void fetchTransactions_ShouldThrowServiceException_WhenFeignExceptionIsThrown() {
        // Given
        TransactionsRequest request = TransactionsRequest.builder().authorization(AUTH_TOKEN).accountId(ACCOUNT_ID).build();
        when(csobApiFeignClient.getTransactions(any(), any(), anyBoolean(), any(), any(), any(), any(), any(), any()))
                .thenThrow(mock(FeignException.class));

        // When & Then
        assertThrows(ServiceException.class, () -> csobApiProvider.fetchTransactions(request, "2026-01-01", "2026-01-31"));
    }

    // --- fetchAccountBalance Tests ---

    @Test
    void fetchAccountBalance_ShouldReturnAmount_WhenApiCallIsSuccessful() {
        // Given
        AccountBalancesMessagingRequest request = AccountBalancesMessagingRequest.builder()
                .authorization(AUTH_TOKEN)
                .accountId(ACCOUNT_ID)
                .build();

        GetAccountBalanceRes mockRes = new GetAccountBalanceRes();
        when(csobApiFeignClient.getAccountBalance(eq(AUTH_TOKEN), anyString(), eq(true), eq(TPP_NAME), eq(API_KEY), eq(JSON_CONTENT_TYPE), eq(ACCOUNT_ID)))
                .thenReturn(ResponseEntity.ok(mockRes));

        Amount expectedAmount = Amount.builder().value(BigDecimal.TEN).currency("CZK").build();
        when(csobApiMapper.toDomainBalance(mockRes)).thenReturn(expectedAmount);

        // When
        Amount actualAmount = csobApiProvider.fetchAccountBalance(request);

        // Then
        assertNotNull(actualAmount);
        assertEquals(expectedAmount, actualAmount);
    }

    @Test
    void fetchAccountBalance_ShouldThrowServiceException_WhenResponseHasNoBody() {
        // Given
        AccountBalancesMessagingRequest request = AccountBalancesMessagingRequest.builder()
                .authorization(AUTH_TOKEN)
                .accountId(ACCOUNT_ID)
                .build();

        when(csobApiFeignClient.getAccountBalance(any(), any(), anyBoolean(), any(), any(), any(), any()))
                .thenReturn(ResponseEntity.badRequest().build());

        // When & Then
        assertThrows(ServiceException.class, () -> csobApiProvider.fetchAccountBalance(request));
    }
}