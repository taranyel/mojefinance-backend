package cvut.fel.sit.mojefinance.product.messaging.service;

import cvut.fel.sit.airbank.openapi.model.AccountList;
import cvut.fel.sit.airbank.openapi.model.TransactionList;
import cvut.fel.sit.cs.openapi.model.MyAccountsIdBalanceGet200Response;
import cvut.fel.sit.csob.transactions.openapi.model.GetTransactionHistoryRes;
import cvut.fel.sit.mojefinance.product.domain.dto.AccountBalancesMessagingRequest;
import cvut.fel.sit.mojefinance.product.domain.dto.ProductsResponse;
import cvut.fel.sit.mojefinance.product.domain.dto.TransactionsRequest;
import cvut.fel.sit.mojefinance.product.domain.entity.Amount;
import cvut.fel.sit.mojefinance.product.domain.entity.BankDetails;
import cvut.fel.sit.mojefinance.product.messaging.client.*;
import cvut.fel.sit.mojefinance.product.messaging.dto.ProductsMessagingRequest;
import cvut.fel.sit.mojefinance.product.messaging.dto.TransactionsMessagingResponse;
import cvut.fel.sit.mojefinance.product.messaging.mapper.AccountBalanceApiMapper;
import cvut.fel.sit.mojefinance.product.messaging.mapper.ProductsApiMapper;
import cvut.fel.sit.mojefinance.product.messaging.mapper.TransactionsApiMapper;
import cvut.fel.sit.shared.exception.ServiceException;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

import static cvut.fel.sit.shared.util.Constants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExternalApiProviderImplTest {

    @Mock private AirBankApiFeignClient airBankApiFeignClient;
    @Mock private CeskaSporitelnaApiFeignClient ceskaSporitelnaApiFeignClient;
    @Mock private CSOBApiFeignClient csobApiFeignClient;
    @Mock private KBApiFeignClient kbApiFeignClient;
    @Mock private RaiffeisenBankApiFeignClient raiffeisenBankApiFeignClient;

    @Mock private ProductsApiMapper productsApiMapper;
    @Mock private AccountBalanceApiMapper accountBalanceApiMapper;
    @Mock private TransactionsApiMapper transactionsApiMapper;

    @InjectMocks
    private ExternalApiProviderImpl externalApiProvider;

    private BankDetails bankDetails;
    private final String AUTH_TOKEN = "Bearer test-token";
    private final String ACCOUNT_ID = "acc-123";
    private final String PRINCIPAL = "user123";

    @BeforeEach
    void setUp() {
        // Inject values normally handled by Spring's @Value
        ReflectionTestUtils.setField(externalApiProvider, "csobApiKey", "csob-key");
        ReflectionTestUtils.setField(externalApiProvider, "csobTppName", "mojefinance");
        ReflectionTestUtils.setField(externalApiProvider, "ceskaSporitelnaApiKey", "cs-key");
        ReflectionTestUtils.setField(externalApiProvider, "raiffeisenBankXIbmClientId", "rb-key");

        bankDetails = new BankDetails();
        bankDetails.setBankName("Test Bank");
    }

    @Test
    void getProducts_AirBank_ShouldRouteAndMapSuccessfully() {
        // Arrange
        bankDetails.setClientRegistrationId(AIR_BANK_CLIENT_REGISTRATION_ID);
        ProductsMessagingRequest request = getProductsMessagingRequest();

        AccountList mockApiResponse = new AccountList();
        ProductsResponse expectedResponse = ProductsResponse.builder().build();

        when(airBankApiFeignClient.getAccounts(AUTH_TOKEN)).thenReturn(ResponseEntity.ok(mockApiResponse));
        when(productsApiMapper.toProductsResponse(mockApiResponse, bankDetails)).thenReturn(expectedResponse);

        // Act
        ProductsResponse actualResponse = externalApiProvider.getProducts(request);

        // Assert
        assertEquals(expectedResponse, actualResponse);
        verify(airBankApiFeignClient).getAccounts(AUTH_TOKEN);
    }

    @Test
    void getProducts_KB_ShouldRouteAndMapSuccessfully() {
        // Arrange
        bankDetails.setClientRegistrationId(KB_CLIENT_REGISTRATION_ID);
        ProductsMessagingRequest request = getProductsMessagingRequest();

        cvut.fel.sit.kb.openapi.model.GetAccountListResponse mockApiResponse = new cvut.fel.sit.kb.openapi.model.GetAccountListResponse();
        ProductsResponse expectedResponse = ProductsResponse.builder().build();

        when(kbApiFeignClient.getAccounts(AUTH_TOKEN)).thenReturn(ResponseEntity.ok(mockApiResponse));
        when(productsApiMapper.toProductsResponse(mockApiResponse, bankDetails)).thenReturn(expectedResponse);

        // Act
        ProductsResponse actualResponse = externalApiProvider.getProducts(request);

        // Assert
        assertEquals(expectedResponse, actualResponse);
        verify(kbApiFeignClient).getAccounts(AUTH_TOKEN);
    }

    @Test
    void getProducts_UnsupportedBank_ShouldThrowIllegalArgumentException() {
        // Arrange
        bankDetails.setClientRegistrationId("UNKNOWN_BANK");
        ProductsMessagingRequest request = getProductsMessagingRequest();

        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> externalApiProvider.getProducts(request));
        assertTrue(ex.getMessage().contains("Unsupported client registration ID"));
    }

    @Test
    void getAccountBalance_CeskaSporitelna_ShouldRouteAndMapSuccessfully() {
        // Arrange
        bankDetails.setClientRegistrationId(CESKA_SPORITELNA_CLIENT_REGISTRATION_ID);
        AccountBalancesMessagingRequest request = AccountBalancesMessagingRequest.builder()
                .accountId(ACCOUNT_ID)
                .authorization(AUTH_TOKEN)
                .bankDetails(bankDetails)
                .principalName(PRINCIPAL)
                .build();

        MyAccountsIdBalanceGet200Response mockApiResponse = new MyAccountsIdBalanceGet200Response();
        Amount expectedAmount = Amount.builder().build();

        when(ceskaSporitelnaApiFeignClient.getAccountBalance(AUTH_TOKEN, "cs-key", ACCOUNT_ID))
                .thenReturn(ResponseEntity.ok(mockApiResponse));
        when(accountBalanceApiMapper.toDomainBalance(mockApiResponse)).thenReturn(expectedAmount);

        // Act
        Amount actualAmount = externalApiProvider.getAccountBalance(request);

        // Assert
        assertEquals(expectedAmount, actualAmount);
        verify(ceskaSporitelnaApiFeignClient).getAccountBalance(AUTH_TOKEN, "cs-key", ACCOUNT_ID);
    }

    @Test
    void getTransactions_CSOB_ShouldFormatDatesToUTCInstant() {
        // Arrange
        bankDetails.setClientRegistrationId(CSOB_CLIENT_REGISTRATION_ID);

        LocalDate fromDate = LocalDate.of(2026, 4, 1);
        LocalDate toDate = LocalDate.of(2026, 4, 19);

        TransactionsRequest request = TransactionsRequest.builder()
                .bankDetails(bankDetails)
                .authorization(AUTH_TOKEN)
                .principalName(PRINCIPAL)
                .accountId(ACCOUNT_ID)
                .fromDate(fromDate)
                .toDate(toDate)
                .build();

        GetTransactionHistoryRes mockApiResponse = new GetTransactionHistoryRes();
        TransactionsMessagingResponse expectedResponse = TransactionsMessagingResponse.builder().build();

        String expectedFromDateIso = "2026-04-01T00:00:00Z";
        String expectedToDateIso = "2026-04-19T00:00:00Z";

        when(csobApiFeignClient.getTransactions(eq(AUTH_TOKEN), anyString(), eq(true), eq("mojefinance"),
                eq("csob-key"), eq("application/json"), eq(ACCOUNT_ID), eq(expectedFromDateIso), eq(expectedToDateIso)))
                .thenReturn(ResponseEntity.ok(mockApiResponse));

        when(transactionsApiMapper.toTransactionsResponse(mockApiResponse)).thenReturn(expectedResponse);

        // Act
        TransactionsMessagingResponse actualResponse = externalApiProvider.getTransactions(request);

        // Assert
        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void getTransactions_Raiffeisen_ShouldPassCustomHeadersAndCurrency() {
        // Arrange
        bankDetails.setClientRegistrationId(RAIFFEISEN_BANK_CLIENT_REGISTRATION_ID);

        LocalDate fromDate = LocalDate.of(2026, 4, 1);
        LocalDate toDate = LocalDate.of(2026, 4, 19);

        TransactionsRequest request = TransactionsRequest.builder()
                .bankDetails(bankDetails)
                .authorization(AUTH_TOKEN)
                .principalName(PRINCIPAL)
                .accountId(ACCOUNT_ID)
                .fromDate(fromDate)
                .toDate(toDate)
                .build();

        cvut.fel.sit.reif.openapi.model.GetTransactionList200Response mockApiResponse = new cvut.fel.sit.reif.openapi.model.GetTransactionList200Response();
        TransactionsMessagingResponse expectedResponse = TransactionsMessagingResponse.builder().build();

        when(raiffeisenBankApiFeignClient.getTransactions(eq("rb-key"), anyString(), eq(ACCOUNT_ID),
                eq(CZK_CURRENCY_CODE), eq(fromDate.toString()), eq(toDate.toString())))
                .thenReturn(ResponseEntity.ok(mockApiResponse));

        when(transactionsApiMapper.toTransactionsResponse(mockApiResponse)).thenReturn(expectedResponse);

        // Act
        TransactionsMessagingResponse actualResponse = externalApiProvider.getTransactions(request);

        // Assert
        assertEquals(expectedResponse, actualResponse);
        verify(raiffeisenBankApiFeignClient).getTransactions(eq("rb-key"), anyString(), eq(ACCOUNT_ID), eq(CZK_CURRENCY_CODE), eq(fromDate.toString()), eq(toDate.toString()));
    }

    @Test
    void getTransactions_NullDates_ShouldDefaultToNowAndMinus3Months() {
        // Arrange
        bankDetails.setClientRegistrationId(AIR_BANK_CLIENT_REGISTRATION_ID);

        TransactionsRequest request = TransactionsRequest.builder()
                .bankDetails(bankDetails)
                .authorization(AUTH_TOKEN)
                .accountId(ACCOUNT_ID)
                .build();

        TransactionList mockApiResponse = new TransactionList();

        String expectedToDate = LocalDate.now().toString();
        String expectedFromDate = LocalDate.now().minusMonths(3).toString();

        when(airBankApiFeignClient.getTransactions(AUTH_TOKEN, ACCOUNT_ID, expectedFromDate, expectedToDate))
                .thenReturn(ResponseEntity.ok(mockApiResponse));

        when(transactionsApiMapper.toTransactionsResponse(mockApiResponse)).thenReturn(TransactionsMessagingResponse.builder().build());

        // Act
        externalApiProvider.getTransactions(request);

        // Assert
        verify(airBankApiFeignClient).getTransactions(AUTH_TOKEN, ACCOUNT_ID, expectedFromDate, expectedToDate);
    }

    @Test
    void executeApiCall_WhenApiReturnsNon2xxStatus_ShouldThrowServiceException() {
        // Arrange
        bankDetails.setClientRegistrationId(AIR_BANK_CLIENT_REGISTRATION_ID);
        ProductsMessagingRequest request = getProductsMessagingRequest();

        when(airBankApiFeignClient.getAccounts(AUTH_TOKEN))
                .thenReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null));

        // Act & Assert
        ServiceException ex = assertThrows(ServiceException.class,
                () -> externalApiProvider.getProducts(request));
        assertTrue(ex.getMessage().contains("Failed to fetch data from Test Bank API. Status: 400 BAD_REQUEST"));
    }

    @Test
    void executeApiCall_WhenFeignThrowsException_ShouldWrapInServiceException() {
        // Arrange
        bankDetails.setClientRegistrationId(AIR_BANK_CLIENT_REGISTRATION_ID);
        ProductsMessagingRequest request = getProductsMessagingRequest();

        FeignException mockFeignException = mock(FeignException.class);
        when(mockFeignException.getMessage()).thenReturn("Connection timed out");

        when(airBankApiFeignClient.getAccounts(AUTH_TOKEN)).thenThrow(mockFeignException);

        // Act & Assert
        ServiceException ex = assertThrows(ServiceException.class,
                () -> externalApiProvider.getProducts(request));
        assertTrue(ex.getMessage().contains("Error calling Test Bank API: Connection timed out"));
        assertEquals(mockFeignException, ex.getCause());
    }

    private ProductsMessagingRequest getProductsMessagingRequest() {
        return ProductsMessagingRequest.builder()
                .authorization(AUTH_TOKEN)
                .principalName(PRINCIPAL)
                .bankDetails(bankDetails)
                .build();
    }
}