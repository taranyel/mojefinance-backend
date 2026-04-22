package cvut.fel.sit.mojefinance.product.messaging.service;

import cvut.fel.sit.cs.openapi.model.MyAccountsGet200Response;
import cvut.fel.sit.cs.openapi.model.MyAccountsIdBalanceGet200Response;
import cvut.fel.sit.cs.openapi.model.MyAccountsIdTransactionsGet200Response;
import cvut.fel.sit.mojefinance.product.domain.dto.AccountBalancesMessagingRequest;
import cvut.fel.sit.mojefinance.product.domain.dto.ProductsResponse;
import cvut.fel.sit.mojefinance.product.domain.dto.TransactionsRequest;
import cvut.fel.sit.mojefinance.product.domain.entity.Amount;
import cvut.fel.sit.mojefinance.product.domain.entity.BankDetails;
import cvut.fel.sit.mojefinance.product.messaging.client.CeskaSporitelnaApiFeignClient;
import cvut.fel.sit.mojefinance.product.messaging.dto.ProductsMessagingRequest;
import cvut.fel.sit.mojefinance.product.messaging.dto.TransactionsMessagingResponse;
import cvut.fel.sit.mojefinance.product.messaging.mapper.CeskaSporitelnaApiMapper;
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

class CeskaSporitelnaApiProviderTest {
    @Mock
    private CeskaSporitelnaApiFeignClient ceskaSporitelnaApiFeignClient;
    @Mock
    private CeskaSporitelnaApiMapper ceskaSporitelnaApiMapper;
    @InjectMocks
    private CeskaSporitelnaApiProvider ceskaSporitelnaApiProvider;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Set the API key field via reflection since @Value is not injected in unit tests
        try {
            java.lang.reflect.Field field = CeskaSporitelnaApiProvider.class.getDeclaredField("ceskaSporitelnaApiKey");
            field.setAccessible(true);
            field.set(ceskaSporitelnaApiProvider, "dummy-api-key");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void fetchProducts_success() {
        ProductsMessagingRequest req = ProductsMessagingRequest.builder()
                .authorization("Bearer token")
                .bankDetails(BankDetails.builder().clientRegistrationId("ceska-sporitelna").build())
                .build();
        MyAccountsGet200Response apiResponse = new MyAccountsGet200Response();
        ProductsResponse productsResponse = new ProductsResponse();
        when(ceskaSporitelnaApiFeignClient.getAccounts(anyString(), anyString())).thenReturn(ResponseEntity.ok(apiResponse));
        when(ceskaSporitelnaApiMapper.toProductsResponse(eq(apiResponse), any())).thenReturn(productsResponse);
        ProductsResponse result = ceskaSporitelnaApiProvider.fetchProducts(req);
        assertSame(productsResponse, result);
    }

    @Test
    void fetchProducts_non2xx_throws() {
        ProductsMessagingRequest req = ProductsMessagingRequest.builder().authorization("Bearer token").bankDetails(BankDetails.builder().build()).build();
        when(ceskaSporitelnaApiFeignClient.getAccounts(anyString(), anyString())).thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
        assertThrows(ServiceException.class, () -> ceskaSporitelnaApiProvider.fetchProducts(req));
    }

    @Test
    void fetchProducts_feignException_throws() {
        ProductsMessagingRequest req = ProductsMessagingRequest.builder().authorization("Bearer token").bankDetails(BankDetails.builder().build()).build();
        when(ceskaSporitelnaApiFeignClient.getAccounts(anyString(), anyString())).thenThrow(mock(FeignException.class));
        assertThrows(ServiceException.class, () -> ceskaSporitelnaApiProvider.fetchProducts(req));
    }

    @Test
    void fetchTransactions_success() {
        TransactionsRequest req = TransactionsRequest.builder().authorization("Bearer token").accountId("accId").build();
        MyAccountsIdTransactionsGet200Response apiResponse = new MyAccountsIdTransactionsGet200Response();
        TransactionsMessagingResponse resp = new TransactionsMessagingResponse();
        when(ceskaSporitelnaApiFeignClient.getTransactions(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(ResponseEntity.ok(apiResponse));
        when(ceskaSporitelnaApiMapper.toTransactionsResponse(apiResponse)).thenReturn(resp);
        TransactionsMessagingResponse result = ceskaSporitelnaApiProvider.fetchTransactions(req, "2024-01-01", "2024-01-31");
        assertSame(resp, result);
    }

    @Test
    void fetchTransactions_non2xx_throws() {
        TransactionsRequest req = TransactionsRequest.builder().authorization("Bearer token").accountId("accId").build();
        when(ceskaSporitelnaApiFeignClient.getTransactions(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
        assertThrows(ServiceException.class, () -> ceskaSporitelnaApiProvider.fetchTransactions(req, "2024-01-01", "2024-01-31"));
    }

    @Test
    void fetchTransactions_feignException_throws() {
        TransactionsRequest req = TransactionsRequest.builder().authorization("Bearer token").accountId("accId").build();
        when(ceskaSporitelnaApiFeignClient.getTransactions(anyString(), anyString(), anyString(), anyString(), anyString())).thenThrow(mock(FeignException.class));
        assertThrows(ServiceException.class, () -> ceskaSporitelnaApiProvider.fetchTransactions(req, "2024-01-01", "2024-01-31"));
    }

    @Test
    void fetchAccountBalance_success() {
        AccountBalancesMessagingRequest req = AccountBalancesMessagingRequest.builder().authorization("Bearer token").accountId("accId").build();
        MyAccountsIdBalanceGet200Response apiResponse = new MyAccountsIdBalanceGet200Response();
        Amount amount = Amount.builder().currency("CZK").build();
        when(ceskaSporitelnaApiFeignClient.getAccountBalance(anyString(), anyString(), anyString())).thenReturn(ResponseEntity.ok(apiResponse));
        when(ceskaSporitelnaApiMapper.toDomainBalance(apiResponse)).thenReturn(amount);
        Amount result = ceskaSporitelnaApiProvider.fetchAccountBalance(req);
        assertSame(amount, result);
    }

    @Test
    void fetchAccountBalance_non2xx_throws() {
        AccountBalancesMessagingRequest req = AccountBalancesMessagingRequest.builder().authorization("Bearer token").accountId("accId").build();
        when(ceskaSporitelnaApiFeignClient.getAccountBalance(anyString(), anyString(), anyString())).thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
        assertThrows(ServiceException.class, () -> ceskaSporitelnaApiProvider.fetchAccountBalance(req));
    }

    @Test
    void fetchAccountBalance_feignException_throws() {
        AccountBalancesMessagingRequest req = AccountBalancesMessagingRequest.builder().authorization("Bearer token").accountId("accId").build();
        when(ceskaSporitelnaApiFeignClient.getAccountBalance(anyString(), anyString(), anyString())).thenThrow(mock(FeignException.class));
        assertThrows(ServiceException.class, () -> ceskaSporitelnaApiProvider.fetchAccountBalance(req));
    }
}
