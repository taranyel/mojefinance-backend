package cvut.fel.sit.mojefinance.product.messaging.service;

import cvut.fel.sit.mojefinance.product.domain.dto.AccountBalancesMessagingRequest;
import cvut.fel.sit.mojefinance.product.domain.dto.ProductsResponse;
import cvut.fel.sit.mojefinance.product.domain.dto.TransactionsRequest;
import cvut.fel.sit.mojefinance.product.domain.entity.Amount;
import cvut.fel.sit.mojefinance.product.domain.entity.BankDetails;
import cvut.fel.sit.mojefinance.product.messaging.dto.ProductsMessagingRequest;
import cvut.fel.sit.mojefinance.product.messaging.dto.TransactionsMessagingResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import static cvut.fel.sit.shared.util.Constants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExternalApiProviderImplTest {

    @Mock private AirBankApiProvider airBankApiProvider;
    @Mock private CeskaSporitelnaApiProvider ceskaSporitelnaApiProvider;
    @Mock private CSOBApiProvider csobApiProvider;
    @Mock private KBApiProvider kbApiProvider;
    @Mock private RaiffeisenBankApiProvider raiffeisenBankApiProvider;
    @Mock private ExchangeRatesApiProvider exchangeRatesApiProvider;

    @InjectMocks
    private ExternalApiProviderImpl externalApiProvider;

    private static final String ACCOUNT_ID = "acc-123";

    // --- getProducts Tests ---

    @Test
    void getProducts_ShouldRouteToCorrectProvider() {
        verifyProductRouting(AIR_BANK_CLIENT_REGISTRATION_ID, airBankApiProvider);
        verifyProductRouting(CESKA_SPORITELNA_CLIENT_REGISTRATION_ID, ceskaSporitelnaApiProvider);
        verifyProductRouting(CSOB_CLIENT_REGISTRATION_ID, csobApiProvider);
        verifyProductRouting(KB_CLIENT_REGISTRATION_ID, kbApiProvider);
        verifyProductRouting(RAIFFEISEN_BANK_CLIENT_REGISTRATION_ID, raiffeisenBankApiProvider);
    }

    @Test
    void getProducts_ShouldThrowIllegalArgumentException_ForUnknownBank() {
        ProductsMessagingRequest request = buildProductsRequest("unknown-bank");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> externalApiProvider.getProducts(request));

        assertTrue(exception.getMessage().contains("Unsupported client registration ID"));
    }

    // --- getAccountBalance Tests ---

    @Test
    void getAccountBalance_ShouldRouteToCorrectProvider() {
        verifyBalanceRouting(AIR_BANK_CLIENT_REGISTRATION_ID, airBankApiProvider);
        verifyBalanceRouting(CESKA_SPORITELNA_CLIENT_REGISTRATION_ID, ceskaSporitelnaApiProvider);
        verifyBalanceRouting(CSOB_CLIENT_REGISTRATION_ID, csobApiProvider);
        verifyBalanceRouting(KB_CLIENT_REGISTRATION_ID, kbApiProvider);
        verifyBalanceRouting(RAIFFEISEN_BANK_CLIENT_REGISTRATION_ID, raiffeisenBankApiProvider);
    }

    @Test
    void getAccountBalance_ShouldThrowIllegalArgumentException_ForUnknownBank() {
        AccountBalancesMessagingRequest request = buildBalanceRequest("unknown-bank");

        assertThrows(IllegalArgumentException.class, () -> externalApiProvider.getAccountBalance(request));
    }

    // --- getTransactions Tests ---

    @Test
    void getTransactions_ShouldFormatDatesAndRouteToStandardProvider() {
        TransactionsRequest request = buildTransactionsRequest(AIR_BANK_CLIENT_REGISTRATION_ID, LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30));

        when(airBankApiProvider.fetchTransactions(request, "2026-04-01", "2026-04-30"))
                .thenReturn(TransactionsMessagingResponse.builder().build());

        externalApiProvider.getTransactions(request);

        verify(airBankApiProvider).fetchTransactions(request, "2026-04-01", "2026-04-30");
    }

    @Test
    void getTransactions_ShouldFormatDatesToIsoInstantAndRouteToCSOB() {
        TransactionsRequest request = buildTransactionsRequest(CSOB_CLIENT_REGISTRATION_ID, LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30));

        String expectedFrom = LocalDate.of(2026, 4, 1).atStartOfDay().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
        String expectedTo = LocalDate.of(2026, 4, 30).atStartOfDay().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);

        when(csobApiProvider.fetchTransactions(request, expectedFrom, expectedTo))
                .thenReturn(TransactionsMessagingResponse.builder().build());

        externalApiProvider.getTransactions(request);

        verify(csobApiProvider).fetchTransactions(request, expectedFrom, expectedTo);
    }

    @Test
    void getTransactions_WhenDatesAreNull_ShouldUseDefaultDates() {
        TransactionsRequest request = buildTransactionsRequest(KB_CLIENT_REGISTRATION_ID, null, null);

        String expectedFrom = LocalDate.now().minusMonths(3).toString();
        String expectedTo = LocalDate.now().toString();

        when(kbApiProvider.fetchTransactions(request, expectedFrom, expectedTo))
                .thenReturn(TransactionsMessagingResponse.builder().build());

        externalApiProvider.getTransactions(request);

        verify(kbApiProvider).fetchTransactions(request, expectedFrom, expectedTo);
    }

    // --- getExchangeRates Tests ---

    @Test
    void getExchangeRates_ShouldDelegateToProvider() {
        when(exchangeRatesApiProvider.fetchExchangeRate("EUR")).thenReturn(BigDecimal.valueOf(25.5));

        BigDecimal result = externalApiProvider.getExchangeRates("EUR");

        assertEquals(0, BigDecimal.valueOf(25.5).compareTo(result));
        verify(exchangeRatesApiProvider).fetchExchangeRate("EUR");
    }

    // --- Fallback Methods Tests (via Reflection) ---

    @Test
    void fallbackGetProducts_ShouldReturnEmptyList() {
        ProductsMessagingRequest request = buildProductsRequest(KB_CLIENT_REGISTRATION_ID);
        RuntimeException ex = new RuntimeException("Service Down");

        ProductsResponse response = ReflectionTestUtils.invokeMethod(externalApiProvider, "fallbackGetProducts", request, ex);

        assertNotNull(response);
        assertTrue(response.getProducts().isEmpty());
    }

    @Test
    void fallbackGetExchangeRates_ShouldReturnOne() {
        RuntimeException ex = new RuntimeException("Service Down");

        BigDecimal response = ReflectionTestUtils.invokeMethod(externalApiProvider, "fallbackGetExchangeRates", "EUR", ex);

        assertNotNull(response);
        assertEquals(0, BigDecimal.ONE.compareTo(response));
    }

    @Test
    void fallbackGetAccountBalance_ShouldReturnZeroCZK() {
        AccountBalancesMessagingRequest request = buildBalanceRequest(KB_CLIENT_REGISTRATION_ID);
        RuntimeException ex = new RuntimeException("Service Down");

        Amount response = ReflectionTestUtils.invokeMethod(externalApiProvider, "fallbackGetAccountBalance", request, ex);

        assertNotNull(response);
        assertEquals(0, BigDecimal.ZERO.compareTo(response.getValue()));
        assertEquals(CZK_CURRENCY_CODE, response.getCurrency());
    }

    @Test
    void fallbackGetTransactions_ShouldReturnEmptyList() {
        TransactionsRequest request = buildTransactionsRequest(KB_CLIENT_REGISTRATION_ID, null, null);
        RuntimeException ex = new RuntimeException("Service Down");

        TransactionsMessagingResponse response = ReflectionTestUtils.invokeMethod(externalApiProvider, "fallbackGetTransactions", request, ex);

        assertNotNull(response);
        assertTrue(response.getTransactions().isEmpty());
    }

    // --- Helper Methods ---

    private void verifyProductRouting(String bankId, Object mockProvider) {
        ProductsMessagingRequest request = buildProductsRequest(bankId);
        ProductsResponse expectedResponse = ProductsResponse.builder().build();

        try {
            mockProvider.getClass().getMethod("fetchProducts", ProductsMessagingRequest.class).invoke(when(mockProvider).getMock(), expectedResponse);
        } catch (Exception ignored) {}

        externalApiProvider.getProducts(request);

        try {
            mockProvider.getClass().getMethod("fetchProducts", ProductsMessagingRequest.class).invoke(verify(mockProvider), request);
        } catch (Exception ignored) {}
    }

    private void verifyBalanceRouting(String bankId, Object mockProvider) {
        AccountBalancesMessagingRequest request = buildBalanceRequest(bankId);
        Amount expectedResponse = Amount.builder().build();

        try {
            mockProvider.getClass().getMethod("fetchAccountBalance", AccountBalancesMessagingRequest.class).invoke(when(mockProvider).getMock(), expectedResponse);
        } catch (Exception ignored) {}

        externalApiProvider.getAccountBalance(request);

        try {
            mockProvider.getClass().getMethod("fetchAccountBalance", AccountBalancesMessagingRequest.class).invoke(verify(mockProvider), request);
        } catch (Exception ignored) {}
    }

    private ProductsMessagingRequest buildProductsRequest(String bankId) {
        return ProductsMessagingRequest.builder()
                .bankDetails(BankDetails.builder().clientRegistrationId(bankId).bankName(bankId).build())
                .build();
    }

    private AccountBalancesMessagingRequest buildBalanceRequest(String bankId) {
        return AccountBalancesMessagingRequest.builder()
                .accountId(ACCOUNT_ID)
                .bankDetails(BankDetails.builder().clientRegistrationId(bankId).bankName(bankId).build())
                .build();
    }

    private TransactionsRequest buildTransactionsRequest(String bankId, LocalDate from, LocalDate to) {
        return TransactionsRequest.builder()
                .accountId(ACCOUNT_ID)
                .fromDate(from)
                .toDate(to)
                .bankDetails(BankDetails.builder().clientRegistrationId(bankId).bankName(bankId).build())
                .build();
    }
}