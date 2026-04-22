package cvut.fel.sit.mojefinance.product.messaging.service;

import cvut.fel.sit.airbank.openapi.model.AccountList;
import cvut.fel.sit.airbank.openapi.model.BalanceList;
import cvut.fel.sit.airbank.openapi.model.TransactionList;
import cvut.fel.sit.mojefinance.product.domain.dto.AccountBalancesMessagingRequest;
import cvut.fel.sit.mojefinance.product.domain.dto.ProductsResponse;
import cvut.fel.sit.mojefinance.product.domain.dto.TransactionsRequest;
import cvut.fel.sit.mojefinance.product.domain.entity.Amount;
import cvut.fel.sit.mojefinance.product.domain.entity.BankDetails;
import cvut.fel.sit.mojefinance.product.messaging.client.AirBankApiFeignClient;
import cvut.fel.sit.mojefinance.product.messaging.dto.ProductsMessagingRequest;
import cvut.fel.sit.mojefinance.product.messaging.dto.TransactionsMessagingResponse;
import cvut.fel.sit.mojefinance.product.messaging.mapper.AirBankApiMapper;
import cvut.fel.sit.shared.exception.ServiceException;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AirBankApiProviderTest {
    @Mock
    private AirBankApiFeignClient airBankApiFeignClient;
    @Mock
    private AirBankApiMapper airBankApiMapper;
    @InjectMocks
    private AirBankApiProvider airBankApiProvider;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void fetchProducts_success() {
        ProductsMessagingRequest req = ProductsMessagingRequest.builder()
                .authorization("Bearer token")
                .bankDetails(BankDetails.builder().clientRegistrationId("air-bank").build())
                .build();
        AccountList accountList = new AccountList();
        ProductsResponse productsResponse = new ProductsResponse();
        when(airBankApiFeignClient.getAccounts(anyString())).thenReturn(ResponseEntity.ok(accountList));
        when(airBankApiMapper.toProductsResponse(eq(accountList), any())).thenReturn(productsResponse);
        ProductsResponse result = airBankApiProvider.fetchProducts(req);
        assertSame(productsResponse, result);
    }

    @Test
    void fetchProducts_non2xx_throws() {
        ProductsMessagingRequest req = ProductsMessagingRequest.builder().authorization("Bearer token").bankDetails(BankDetails.builder().build()).build();
        when(airBankApiFeignClient.getAccounts(anyString())).thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
        assertThrows(ServiceException.class, () -> airBankApiProvider.fetchProducts(req));
    }

    @Test
    void fetchProducts_feignException_throws() {
        ProductsMessagingRequest req = ProductsMessagingRequest.builder().authorization("Bearer token").bankDetails(BankDetails.builder().build()).build();
        when(airBankApiFeignClient.getAccounts(anyString())).thenThrow(mock(FeignException.class));
        assertThrows(ServiceException.class, () -> airBankApiProvider.fetchProducts(req));
    }

    @Test
    void fetchTransactions_success() {
        TransactionsRequest req = TransactionsRequest.builder().authorization("Bearer token").accountId("accId").build();
        TransactionList transactionList = new TransactionList();
        TransactionsMessagingResponse resp = new TransactionsMessagingResponse();
        when(airBankApiFeignClient.getTransactions(anyString(), anyString(), anyString(), anyString())).thenReturn(ResponseEntity.ok(transactionList));
        when(airBankApiMapper.toTransactionsResponse(transactionList)).thenReturn(resp);
        TransactionsMessagingResponse result = airBankApiProvider.fetchTransactions(req, "2024-01-01", "2024-01-31");
        assertSame(resp, result);
    }

    @Test
    void fetchTransactions_non2xx_throws() {
        TransactionsRequest req = TransactionsRequest.builder().authorization("Bearer token").accountId("accId").build();
        when(airBankApiFeignClient.getTransactions(anyString(), anyString(), anyString(), anyString())).thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
        assertThrows(ServiceException.class, () -> airBankApiProvider.fetchTransactions(req, "2024-01-01", "2024-01-31"));
    }

    @Test
    void fetchTransactions_feignException_throws() {
        TransactionsRequest req = TransactionsRequest.builder().authorization("Bearer token").accountId("accId").build();
        when(airBankApiFeignClient.getTransactions(anyString(), anyString(), anyString(), anyString())).thenThrow(mock(FeignException.class));
        assertThrows(ServiceException.class, () -> airBankApiProvider.fetchTransactions(req, "2024-01-01", "2024-01-31"));
    }

    @Test
    void fetchAccountBalance_success() {
        AccountBalancesMessagingRequest req = AccountBalancesMessagingRequest.builder().authorization("Bearer token").accountId("accId").build();
        BalanceList balanceList = new BalanceList();
        Amount amount = Amount.builder().currency("CZK").build();
        when(airBankApiFeignClient.getAccountBalance(anyString(), anyString())).thenReturn(ResponseEntity.ok(balanceList));
        when(airBankApiMapper.toDomainBalance(balanceList)).thenReturn(amount);
        Amount result = airBankApiProvider.fetchAccountBalance(req);
        assertSame(amount, result);
    }

    @Test
    void fetchAccountBalance_non2xx_throws() {
        AccountBalancesMessagingRequest req = AccountBalancesMessagingRequest.builder().authorization("Bearer token").accountId("accId").build();
        when(airBankApiFeignClient.getAccountBalance(anyString(), anyString())).thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
        assertThrows(ServiceException.class, () -> airBankApiProvider.fetchAccountBalance(req));
    }

    @Test
    void fetchAccountBalance_feignException_throws() {
        AccountBalancesMessagingRequest req = AccountBalancesMessagingRequest.builder().authorization("Bearer token").accountId("accId").build();
        when(airBankApiFeignClient.getAccountBalance(anyString(), anyString())).thenThrow(mock(FeignException.class));
        assertThrows(ServiceException.class, () -> airBankApiProvider.fetchAccountBalance(req));
    }
}
