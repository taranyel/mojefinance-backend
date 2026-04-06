package cvut.fel.sit.mojefinance.product.messaging.service;

import cvut.fel.sit.mojefinance.product.domain.dto.AccountBalancesMessagingRequest;
import cvut.fel.sit.mojefinance.product.domain.dto.ProductsDomainResponse;
import cvut.fel.sit.mojefinance.product.domain.dto.TransactionsRequest;
import cvut.fel.sit.mojefinance.product.domain.entity.Amount;
import cvut.fel.sit.mojefinance.product.domain.entity.BankDetails;
import cvut.fel.sit.mojefinance.product.messaging.client.*;
import cvut.fel.sit.mojefinance.product.messaging.dto.ProductsMessagingRequest;
import cvut.fel.sit.mojefinance.product.messaging.dto.TransactionsMessagingResponse;
import cvut.fel.sit.mojefinance.product.messaging.mapper.AccountBalanceApiMapper;
import cvut.fel.sit.mojefinance.product.messaging.mapper.ProductsApiMapper;
import cvut.fel.sit.mojefinance.product.messaging.mapper.TransactionsApiMapper;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import static cvut.fel.sit.shared.util.Constants.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExternalApiProviderImpl implements ExternalApiProvider {
    private final AirBankApiFeignClient airBankApiFeignClient;
    private final CeskaSporitelnaApiFeignClient ceskaSporitelnaApiFeignClient;
    private final CSOBApiFeignClient csobApiFeignClient;
    private final KBApiFeignClient kbApiFeignClient;
    private final RaiffeisenBankApiFeignClient raiffeisenBankApiFeignClient;
    private final ProductsApiMapper productsApiMapper;
    private final AccountBalanceApiMapper accountBalanceApiMapper;
    private final TransactionsApiMapper transactionsApiMapper;

    private static final String APPLICATION_JSON_CONTENT_TYPE = "application/json";

    @Value("${external.api.csob.apikey}")
    private String csobApiKey;

    @Value("${external.api.csob.tpp-name}")
    private String csobTppName;

    @Value("${external.api.ceska-sporitelna.apikey}")
    private String ceskaSporitelnaApiKey;

    @Value("${external.api.raiffeisen-bank.x-ibm-client-id}")
    private String raiffeisenBankXIbmClientId;

    @Override
    @Cacheable(value = "products", key = "#request.principalName + '-' + #request.bankDetails.clientRegistrationId")
    public ProductsDomainResponse getProducts(ProductsMessagingRequest request) {
        BankDetails bankDetails = request.getBankDetails();
        log.info("Fetching products from bank: {}", bankDetails.getClientRegistrationId());

        String clientRegistrationId = bankDetails.getClientRegistrationId();
        String authorization = request.getAuthorization();
        String requestId = UUID.randomUUID().toString();

        return switch (clientRegistrationId) {
            case AIR_BANK_CLIENT_REGISTRATION_ID -> executeApiCall(
                    () -> airBankApiFeignClient.getAccounts(authorization),
                    response -> productsApiMapper.toProductsResponse(response, bankDetails),
                    bankDetails.getBankName()
            );

            case CESKA_SPORITELNA_CLIENT_REGISTRATION_ID -> executeApiCall(
                    () -> ceskaSporitelnaApiFeignClient.getAccounts(authorization, ceskaSporitelnaApiKey),
                    response -> productsApiMapper.toProductsResponse(response, bankDetails),
                    bankDetails.getBankName()
            );

            case CSOB_CLIENT_REGISTRATION_ID -> executeApiCall(
                    () -> csobApiFeignClient.getAccounts(authorization, requestId, true, csobTppName, csobApiKey, APPLICATION_JSON_CONTENT_TYPE),
                    response -> productsApiMapper.toProductsResponse(response, bankDetails),
                    bankDetails.getBankName()
            );

            case KB_CLIENT_REGISTRATION_ID -> executeApiCall(
                    () -> kbApiFeignClient.getAccounts(authorization),
                    response -> productsApiMapper.toProductsResponse(response, bankDetails),
                    bankDetails.getBankName()
            );

            case RAIFFEISEN_BANK_CLIENT_REGISTRATION_ID -> executeApiCall(
                    () -> raiffeisenBankApiFeignClient.getAccounts(raiffeisenBankXIbmClientId, requestId),
                    response -> productsApiMapper.toProductsResponse(response, bankDetails),
                    bankDetails.getBankName()
            );

            default ->
                    throw new IllegalArgumentException("Unsupported client registration ID: " + clientRegistrationId);
        };
    }

    @Override
    @Cacheable(value = "balances", key = "#request.principalName + '-' + #request.bankDetails.clientRegistrationId + '-' + #request.accountId")
    public Amount getAccountBalance(AccountBalancesMessagingRequest request) {
        String clientRegistrationId = request.getBankDetails().getClientRegistrationId();
        String bankName = request.getBankDetails().getBankName();
        log.info("Fetching balance from bank with client id: {}", clientRegistrationId);

        String authorization = request.getAuthorization();
        String requestId = UUID.randomUUID().toString();
        String accountId = request.getAccountId();

        return switch (clientRegistrationId) {
            case AIR_BANK_CLIENT_REGISTRATION_ID -> executeApiCall(
                    () -> airBankApiFeignClient.getAccountBalance(authorization, accountId),
                    accountBalanceApiMapper::toDomainBalance,
                    bankName
            );

            case CESKA_SPORITELNA_CLIENT_REGISTRATION_ID -> executeApiCall(
                    () -> ceskaSporitelnaApiFeignClient.getAccountBalance(authorization, ceskaSporitelnaApiKey, accountId),
                    accountBalanceApiMapper::toDomainBalance,
                    bankName
            );

            case CSOB_CLIENT_REGISTRATION_ID -> executeApiCall(
                    () -> csobApiFeignClient.getAccountBalance(authorization, requestId, true, csobTppName, csobApiKey, APPLICATION_JSON_CONTENT_TYPE, accountId),
                    accountBalanceApiMapper::toDomainBalance,
                    bankName
            );

            case KB_CLIENT_REGISTRATION_ID -> executeApiCall(
                    () -> kbApiFeignClient.getAccountBalance(authorization, accountId),
                    accountBalanceApiMapper::toDomainBalance,
                    bankName
            );

            case RAIFFEISEN_BANK_CLIENT_REGISTRATION_ID -> executeApiCall(
                    () -> raiffeisenBankApiFeignClient.getAccountBalance(raiffeisenBankXIbmClientId, requestId, accountId),
                    accountBalanceApiMapper::toDomainBalance,
                    bankName
            );

            default ->
                    throw new IllegalArgumentException("Unsupported client registration ID: " + clientRegistrationId);
        };
    }

    @Override
    @Cacheable(value = "transactions", key = "#request.principalName + '-' + #request.bankDetails.clientRegistrationId + '-' + #request.accountId")
    public TransactionsMessagingResponse getTransactions(TransactionsRequest request) {
        String clientRegistrationId = request.getBankDetails().getClientRegistrationId();
        String bankName = request.getBankDetails().getBankName();
        log.info("Fetching transactions from bank with client id: {}", clientRegistrationId);

        String authorization = request.getAuthorization();
        String requestId = UUID.randomUUID().toString();
        String accountId = request.getAccountId();

        LocalDate fromDate = getFromDate(request.getFromDate());
        LocalDate toDate = getToDate(request.getToDate());

        return switch (clientRegistrationId) {
            case AIR_BANK_CLIENT_REGISTRATION_ID -> executeApiCall(
                    () -> airBankApiFeignClient.getTransactions(authorization, accountId, fromDate.toString(), toDate.toString()),
                    transactionsApiMapper::toTransactionsResponse,
                    bankName
            );

            case CESKA_SPORITELNA_CLIENT_REGISTRATION_ID -> executeApiCall(
                    () -> ceskaSporitelnaApiFeignClient.getTransactions(authorization, ceskaSporitelnaApiKey,
                            accountId, fromDate.toString(), toDate.toString()),
                    transactionsApiMapper::toTransactionsResponse,
                    bankName
            );

            case CSOB_CLIENT_REGISTRATION_ID -> executeApiCall(
                    () -> {
                        String fromDateTime = fromDate.atStartOfDay()
                                .atOffset(ZoneOffset.UTC)
                                .format(DateTimeFormatter.ISO_INSTANT);
                        String toDateTime = toDate.atStartOfDay()
                                .atOffset(ZoneOffset.UTC)
                                .format(DateTimeFormatter.ISO_INSTANT);
                        return csobApiFeignClient.getTransactions(authorization, requestId, true,
                                csobTppName, csobApiKey, APPLICATION_JSON_CONTENT_TYPE, accountId,
                                fromDateTime, toDateTime);
                    },
                    transactionsApiMapper::toTransactionsResponse,
                    bankName
            );

            case KB_CLIENT_REGISTRATION_ID -> executeApiCall(
                    () -> kbApiFeignClient.getTransactions(authorization, accountId, fromDate.toString(), toDate.toString()),
                    transactionsApiMapper::toTransactionsResponse,
                    bankName
            );

            case RAIFFEISEN_BANK_CLIENT_REGISTRATION_ID -> executeApiCall(
                    () -> raiffeisenBankApiFeignClient.getTransactions(raiffeisenBankXIbmClientId, requestId,
                            accountId, CZK_CURRENCY_CODE, fromDate.toString(), toDate.toString()),
                    transactionsApiMapper::toTransactionsResponse,
                    bankName
            );

            default ->
                    throw new IllegalArgumentException("Unsupported client registration ID: " + clientRegistrationId);
        };
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

    private <T, R> R executeApiCall(Supplier<ResponseEntity<T>> apiCall, Function<T, R> mapper, String bankName) {
        try {
            ResponseEntity<T> response = apiCall.get();

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return mapper.apply(response.getBody());
            } else {
                throw new RuntimeException("Failed to fetch data from " + bankName + " API. Status: " + response.getStatusCode());
            }
        } catch (FeignException e) {
            throw new RuntimeException("Error calling " + bankName + " API: " + e.getMessage(), e);
        }
    }
}