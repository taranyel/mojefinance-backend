package cvut.fel.sit.mojefinance.product.messaging.service;

import cvut.fel.sit.kb.openapi.model.GeAccountTransactionsResponse;
import cvut.fel.sit.kb.openapi.model.GetAccountBalanceResponse;
import cvut.fel.sit.kb.openapi.model.GetAccountListResponse;
import cvut.fel.sit.mojefinance.product.domain.dto.AccountBalancesMessagingRequest;
import cvut.fel.sit.mojefinance.product.domain.dto.ProductsResponse;
import cvut.fel.sit.mojefinance.product.domain.dto.TransactionsRequest;
import cvut.fel.sit.mojefinance.product.domain.entity.Amount;
import cvut.fel.sit.mojefinance.product.domain.entity.BankDetails;
import cvut.fel.sit.mojefinance.product.messaging.client.KBApiFeignClient;
import cvut.fel.sit.mojefinance.product.messaging.dto.ProductsMessagingRequest;
import cvut.fel.sit.mojefinance.product.messaging.dto.TransactionsMessagingResponse;
import cvut.fel.sit.mojefinance.product.messaging.mapper.KBApiMapper;
import cvut.fel.sit.shared.exception.ServiceException;
import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KBApiProviderTest {

    @Mock
    private KBApiFeignClient kbApiFeignClient;

    @Mock
    private KBApiMapper kbApiMapper;

    @InjectMocks
    private KBApiProvider kbApiProvider;

    private static final String AUTH_TOKEN = "Bearer kb-token";
    private static final String ACCOUNT_ID = "kb-acc-123";

    // --- fetchProducts Tests ---

    @Test
    void fetchProducts_ShouldReturnProductsResponse_WhenApiCallIsSuccessful() {
        // Given
        BankDetails bankDetails = BankDetails.builder().bankName("Komerční banka").build();
        ProductsMessagingRequest request = ProductsMessagingRequest.builder()
                .authorization(AUTH_TOKEN)
                .bankDetails(bankDetails)
                .build();

        GetAccountListResponse mockRes = new GetAccountListResponse();
        when(kbApiFeignClient.getAccounts(AUTH_TOKEN))
                .thenReturn(ResponseEntity.ok(mockRes));

        ProductsResponse expectedResponse = ProductsResponse.builder().build();
        when(kbApiMapper.toProductsResponse(mockRes, bankDetails)).thenReturn(expectedResponse);

        // When
        ProductsResponse actualResponse = kbApiProvider.fetchProducts(request);

        // Then
        assertNotNull(actualResponse);
        assertEquals(expectedResponse, actualResponse);
        verify(kbApiMapper).toProductsResponse(mockRes, bankDetails);
    }

    @Test
    void fetchProducts_ShouldThrowServiceException_WhenResponseHasNoBody() {
        // Given
        ProductsMessagingRequest request = ProductsMessagingRequest.builder()
                .authorization(AUTH_TOKEN)
                .build();

        // Returns 200 OK but null body
        when(kbApiFeignClient.getAccounts(AUTH_TOKEN))
                .thenReturn(ResponseEntity.ok().build());

        // When & Then
        ServiceException exception = assertThrows(ServiceException.class, () -> kbApiProvider.fetchProducts(request));
        assertTrue(exception.getMessage().contains("Failed to fetch products from KB API"));
        verifyNoInteractions(kbApiMapper);
    }

    @Test
    void fetchProducts_ShouldThrowServiceException_WhenFeignExceptionIsThrown() {
        // Given
        ProductsMessagingRequest request = ProductsMessagingRequest.builder()
                .authorization(AUTH_TOKEN)
                .build();

        FeignException mockFeignException = mock(FeignException.class);
        when(mockFeignException.getMessage()).thenReturn("Unauthorized");

        when(kbApiFeignClient.getAccounts(AUTH_TOKEN)).thenThrow(mockFeignException);

        // When & Then
        ServiceException exception = assertThrows(ServiceException.class, () -> kbApiProvider.fetchProducts(request));
        assertTrue(exception.getMessage().contains("Error calling KB API"));
    }

    // --- fetchTransactions Tests ---

    @Test
    void fetchTransactions_ShouldReturnTransactionsResponse_WhenApiCallIsSuccessful() {
        // Given
        TransactionsRequest request = TransactionsRequest.builder()
                .authorization(AUTH_TOKEN)
                .accountId(ACCOUNT_ID)
                .build();
        String fromDate = "2026-04-01";
        String toDate = "2026-04-30";

        GeAccountTransactionsResponse mockRes = new GeAccountTransactionsResponse();
        when(kbApiFeignClient.getTransactions(AUTH_TOKEN, ACCOUNT_ID, fromDate, toDate))
                .thenReturn(ResponseEntity.ok(mockRes));

        TransactionsMessagingResponse expectedResponse = TransactionsMessagingResponse.builder().build();
        when(kbApiMapper.toTransactionsResponse(mockRes)).thenReturn(expectedResponse);

        // When
        TransactionsMessagingResponse actualResponse = kbApiProvider.fetchTransactions(request, fromDate, toDate);

        // Then
        assertNotNull(actualResponse);
        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void fetchTransactions_ShouldThrowServiceException_WhenFeignExceptionIsThrown() {
        // Given
        TransactionsRequest request = TransactionsRequest.builder()
                .authorization(AUTH_TOKEN)
                .accountId(ACCOUNT_ID)
                .build();

        when(kbApiFeignClient.getTransactions(any(), any(), any(), any()))
                .thenThrow(mock(FeignException.class));

        // When & Then
        assertThrows(ServiceException.class, () -> kbApiProvider.fetchTransactions(request, "2026-04-01", "2026-04-30"));
    }

    // --- fetchAccountBalance Tests ---

    @Test
    void fetchAccountBalance_ShouldReturnAmount_WhenApiCallIsSuccessful() {
        // Given
        AccountBalancesMessagingRequest request = AccountBalancesMessagingRequest.builder()
                .authorization(AUTH_TOKEN)
                .accountId(ACCOUNT_ID)
                .build();

        GetAccountBalanceResponse mockRes = new GetAccountBalanceResponse();
        when(kbApiFeignClient.getAccountBalance(AUTH_TOKEN, ACCOUNT_ID))
                .thenReturn(ResponseEntity.ok(mockRes));

        Amount expectedAmount = Amount.builder().value(BigDecimal.valueOf(1500)).currency("CZK").build();
        when(kbApiMapper.toDomainBalance(mockRes)).thenReturn(expectedAmount);

        // When
        Amount actualAmount = kbApiProvider.fetchAccountBalance(request);

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

        // 400 Bad Request
        when(kbApiFeignClient.getAccountBalance(any(), any()))
                .thenReturn(ResponseEntity.badRequest().build());

        // When & Then
        assertThrows(ServiceException.class, () -> kbApiProvider.fetchAccountBalance(request));
    }
}