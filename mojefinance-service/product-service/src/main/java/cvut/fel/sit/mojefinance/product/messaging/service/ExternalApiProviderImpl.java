package cvut.fel.sit.mojefinance.product.messaging.service;

import cvut.fel.sit.mojefinance.product.domain.dto.AccountBalancesMessagingRequest;
import cvut.fel.sit.mojefinance.product.domain.dto.ProductsResponse;
import cvut.fel.sit.mojefinance.product.domain.dto.TransactionsRequest;
import cvut.fel.sit.mojefinance.product.domain.entity.Amount;
import cvut.fel.sit.mojefinance.product.messaging.dto.ProductsMessagingRequest;
import cvut.fel.sit.mojefinance.product.messaging.dto.TransactionsMessagingResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

import static cvut.fel.sit.shared.util.Constants.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExternalApiProviderImpl implements ExternalApiProvider {
    private final AirBankApiProvider airBankApiProvider;
    private final CeskaSporitelnaApiProvider ceskaSporitelnaApiProvider;
    private final CSOBApiProvider csobApiProvider;
    private final KBApiProvider kbApiProvider;
    private final RaiffeisenBankApiProvider raiffeisenBankApiProvider;
    private final ExchangeRatesApiProvider exchangeRatesApiProvider;
    private static final String RESILIENCE_INSTANCE = "bankApi";

    @Override
    @Retry(name = RESILIENCE_INSTANCE, fallbackMethod = "fallbackGetProducts")
    @CircuitBreaker(name = RESILIENCE_INSTANCE, fallbackMethod = "fallbackGetProducts")
    @Cacheable(value = "products", key = "#request.principalName + '-' + #request.bankDetails.clientRegistrationId")
    public ProductsResponse getProducts(ProductsMessagingRequest request) {
        String clientRegistrationId = request.getBankDetails().getClientRegistrationId();
        log.info("Fetching products from bank: {}", clientRegistrationId);

        return switch (clientRegistrationId) {
            case AIR_BANK_CLIENT_REGISTRATION_ID -> airBankApiProvider.fetchProducts(request);
            case CESKA_SPORITELNA_CLIENT_REGISTRATION_ID -> ceskaSporitelnaApiProvider.fetchProducts(request);
            case CSOB_CLIENT_REGISTRATION_ID -> csobApiProvider.fetchProducts(request);
            case KB_CLIENT_REGISTRATION_ID -> kbApiProvider.fetchProducts(request);
            case RAIFFEISEN_BANK_CLIENT_REGISTRATION_ID -> raiffeisenBankApiProvider.fetchProducts(request);
            default ->
                    throw new IllegalArgumentException("Unsupported client registration ID: " + clientRegistrationId);
        };
    }

    @Override
    @Retry(name = RESILIENCE_INSTANCE, fallbackMethod = "fallbackGetAccountBalance")
    @CircuitBreaker(name = RESILIENCE_INSTANCE, fallbackMethod = "fallbackGetAccountBalance")
    @Cacheable(value = "balances", key = "#request.principalName + '-' + #request.bankDetails.clientRegistrationId + '-' + #request.accountId")
    public Amount getAccountBalance(AccountBalancesMessagingRequest request) {
        String clientRegistrationId = request.getBankDetails().getClientRegistrationId();
        log.info("Fetching balance from bank with client id: {}", clientRegistrationId);

        return switch (clientRegistrationId) {
            case AIR_BANK_CLIENT_REGISTRATION_ID -> airBankApiProvider.fetchAccountBalance(request);
            case CESKA_SPORITELNA_CLIENT_REGISTRATION_ID -> ceskaSporitelnaApiProvider.fetchAccountBalance(request);
            case CSOB_CLIENT_REGISTRATION_ID -> csobApiProvider.fetchAccountBalance(request);
            case KB_CLIENT_REGISTRATION_ID -> kbApiProvider.fetchAccountBalance(request);
            case RAIFFEISEN_BANK_CLIENT_REGISTRATION_ID -> raiffeisenBankApiProvider.fetchAccountBalance(request);

            default ->
                    throw new IllegalArgumentException("Unsupported client registration ID: " + clientRegistrationId);
        };
    }

    @Override
    @Retry(name = RESILIENCE_INSTANCE, fallbackMethod = "fallbackGetTransactions")
    @CircuitBreaker(name = RESILIENCE_INSTANCE, fallbackMethod = "fallbackGetTransactions")
    @Cacheable(value = "transactions", key = "#request.principalName + '-' + #request.bankDetails.clientRegistrationId + '-' + #request.accountId")
    public TransactionsMessagingResponse getTransactions(TransactionsRequest request) {
        String clientRegistrationId = request.getBankDetails().getClientRegistrationId();
        log.info("Fetching transactions from bank with client id: {}", clientRegistrationId);

        LocalDate fromDate = getFromDate(request.getFromDate());
        LocalDate toDate = getToDate(request.getToDate());

        return switch (clientRegistrationId) {
            case AIR_BANK_CLIENT_REGISTRATION_ID ->
                    airBankApiProvider.fetchTransactions(request, fromDate.toString(), toDate.toString());
            case CESKA_SPORITELNA_CLIENT_REGISTRATION_ID ->
                    ceskaSporitelnaApiProvider.fetchTransactions(request, fromDate.toString(), toDate.toString());

            case CSOB_CLIENT_REGISTRATION_ID -> {
                String fromDateTime = fromDate.atStartOfDay()
                        .atOffset(ZoneOffset.UTC)
                        .format(DateTimeFormatter.ISO_INSTANT);
                String toDateTime = toDate.atStartOfDay()
                        .atOffset(ZoneOffset.UTC)
                        .format(DateTimeFormatter.ISO_INSTANT);
                yield csobApiProvider.fetchTransactions(request, fromDateTime, toDateTime);
            }
            case KB_CLIENT_REGISTRATION_ID ->
                    kbApiProvider.fetchTransactions(request, fromDate.toString(), toDate.toString());
            case RAIFFEISEN_BANK_CLIENT_REGISTRATION_ID ->
                    raiffeisenBankApiProvider.fetchTransactions(request, fromDate.toString(), toDate.toString());
            default ->
                    throw new IllegalArgumentException("Unsupported client registration ID: " + clientRegistrationId);
        };
    }

    @Override
    @Retry(name = RESILIENCE_INSTANCE, fallbackMethod = "fallbackGetExchangeRates")
    @CircuitBreaker(name = RESILIENCE_INSTANCE, fallbackMethod = "fallbackGetExchangeRates")
    @Cacheable(value = "exchange-rates", key = "#currency")
    public BigDecimal getExchangeRates(String currency) {
        log.info("Fetching exchange rates from KB for: {}.", currency);
        return exchangeRatesApiProvider.fetchExchangeRate(currency);
    }

    private ProductsResponse fallbackGetProducts(ProductsMessagingRequest request, Throwable throwable) {
        log.error("CircuitBreaker/Retry Fallback triggered for getProducts. Bank: {}. Reason: {}",
                request.getBankDetails().getBankName(), throwable.getMessage());
        return ProductsResponse.builder()
                .products(Collections.emptyList())
                .build();
    }

    private BigDecimal fallbackGetExchangeRates(String currency, Throwable throwable) {
        return BigDecimal.ONE;
    }

    private Amount fallbackGetAccountBalance(AccountBalancesMessagingRequest request, Throwable throwable) {
        log.error("CircuitBreaker/Retry Fallback triggered for getAccountBalance. Bank: {}. Reason: {}",
                request.getBankDetails().getBankName(), throwable.getMessage());
        return Amount.builder()
                .value(BigDecimal.ZERO)
                .currency(CZK_CURRENCY_CODE)
                .build();
    }

    private TransactionsMessagingResponse fallbackGetTransactions(TransactionsRequest request, Throwable throwable) {
        log.error("CircuitBreaker/Retry Fallback triggered for getTransactions. Bank: {}. Reason: {}",
                request.getBankDetails().getBankName(), throwable.getMessage());
        return TransactionsMessagingResponse.builder()
                .transactions(Collections.emptyList())
                .build();
    }

    private LocalDate getToDate(LocalDate toDate) {
        if (toDate == null) {
            toDate = LocalDate.now();
        }
        return toDate;
    }

    private LocalDate getFromDate(LocalDate fromDate) {
        if (fromDate == null) {
            fromDate = LocalDate.now().minusMonths(3);
        }
        return fromDate;
    }
}