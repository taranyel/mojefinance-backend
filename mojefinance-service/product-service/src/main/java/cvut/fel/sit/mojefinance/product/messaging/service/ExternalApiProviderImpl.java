package cvut.fel.sit.mojefinance.product.messaging.service;

import cvut.fel.sit.mojefinance.product.domain.dto.GetProductsResponse;
import cvut.fel.sit.mojefinance.product.domain.entity.Balance;
import cvut.fel.sit.mojefinance.product.domain.entity.BankDetails;
import cvut.fel.sit.mojefinance.product.messaging.client.KBApiFeignClient;
import cvut.fel.sit.mojefinance.product.messaging.client.CSOBApiFeignClient;
import cvut.fel.sit.mojefinance.product.messaging.client.ReiffeisenBankApiFeignClient;
import cvut.fel.sit.mojefinance.product.messaging.client.AirBankApiFeignClient;
import cvut.fel.sit.mojefinance.product.messaging.client.CeskaSporitelnaApiFeignClient;
import cvut.fel.sit.mojefinance.product.messaging.dto.AccountBalancesMessagingRequest;
import cvut.fel.sit.mojefinance.product.messaging.dto.ProductsMessagingRequest;
import cvut.fel.sit.mojefinance.product.messaging.mapper.AccountBalanceApiMapper;
import cvut.fel.sit.mojefinance.product.messaging.mapper.ProductsApiMapper;
import cvut.fel.sit.shared.util.Constants;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExternalApiProviderImpl implements ExternalApiProvider {
    private final AirBankApiFeignClient airBankApiFeignClient;
    private final CeskaSporitelnaApiFeignClient ceskaSporitelnaApiFeignClient;
    private final CSOBApiFeignClient csobApiFeignClient;
    private final KBApiFeignClient kbApiFeignClient;
    private final ReiffeisenBankApiFeignClient reiffeisenBankApiFeignClient;
    private final ProductsApiMapper productsApiMapper;
    private final AccountBalanceApiMapper accountBalanceApiMapper;

    private static final String CONTENT_TYPE = "application/json";

    @Value("${external.api.csob.apikey}")
    private String csobApiKey;

    @Value("${external.api.csob.tpp-name}")
    private String csobTppName;

    @Value("${external.api.ceska-sporitelna.apikey}")
    private String ceskaSporitelnaApiKey;

    @Value("${external.api.reiffeisen-bank.x-ibm-client-id}")
    private String reiffeisenBankXIbmClientId;

    @Override
    @Cacheable(value = "products", key = "#request.bankDetails.clientRegistrationId")
    public GetProductsResponse getProducts(ProductsMessagingRequest request) {
        BankDetails bankDetails = request.getBankDetails();
        log.info("Fetching products from bank: {}", request.getBankDetails());

        String clientRegistrationId = bankDetails.getClientRegistrationId();
        String authorization = request.getAuthorization();
        String requestId = UUID.randomUUID().toString();

        return switch (clientRegistrationId) {
            case Constants.AIR_BANK_CLIENT_REGISTRATION_ID -> executeGetProductsApiCall(
                    () -> airBankApiFeignClient.getAccounts(authorization),
                    productsApiMapper::toGetProductsResponse,
                    bankDetails
            );

            case Constants.CESKA_SPORITELNA_CLIENT_REGISTRATION_ID -> executeGetProductsApiCall(
                    () -> ceskaSporitelnaApiFeignClient.getAccounts(authorization, ceskaSporitelnaApiKey),
                    productsApiMapper::toGetProductsResponse,
                    bankDetails
            );

            case Constants.CSOB_CLIENT_REGISTRATION_ID -> executeGetProductsApiCall(
                    () -> csobApiFeignClient.getAccounts(authorization, requestId, true, csobTppName, csobApiKey, CONTENT_TYPE),
                    productsApiMapper::toGetProductsResponse,
                    bankDetails
            );

            case Constants.KB_CLIENT_REGISTRATION_ID -> executeGetProductsApiCall(
                    () -> kbApiFeignClient.getAccounts(authorization),
                    productsApiMapper::toGetProductsResponse,
                    bankDetails
            );

            case Constants.REIFFEISEN_BANK_CLIENT_REGISTRATION_ID -> executeGetProductsApiCall(
                    () -> reiffeisenBankApiFeignClient.getAccounts(reiffeisenBankXIbmClientId, requestId),
                    productsApiMapper::toGetProductsResponse,
                    bankDetails
            );

            default ->
                    throw new IllegalArgumentException("Unsupported client registration ID: " + clientRegistrationId);
        };
    }

    @Override
    @Cacheable(value = "balances", key = "#request.bankDetails.clientRegistrationId + '-' + #request.accountId")
    public Balance getAccountBalance(AccountBalancesMessagingRequest request) {
        String clientRegistrationId = request.getBankDetails().getClientRegistrationId();
        log.info("Fetching products from bank with client id: {}", request.getBankDetails().getClientRegistrationId());

        String authorization = request.getAuthorization();
        String requestId = UUID.randomUUID().toString();
        String bankName = request.getBankDetails().getBankName();
        String accountId = request.getAccountId();

        return switch (clientRegistrationId) {
            case Constants.AIR_BANK_CLIENT_REGISTRATION_ID -> executeGetAccountBalanceApiCall(
                    () -> airBankApiFeignClient.getAccountBalance(authorization, accountId),
                    accountBalanceApiMapper::toDomainBalance,
                    bankName
            );

            case Constants.CESKA_SPORITELNA_CLIENT_REGISTRATION_ID -> executeGetAccountBalanceApiCall(
                    () -> ceskaSporitelnaApiFeignClient.getAccountBalance(authorization, ceskaSporitelnaApiKey, accountId),
                    accountBalanceApiMapper::toDomainBalance,
                    bankName
            );

            case Constants.CSOB_CLIENT_REGISTRATION_ID -> executeGetAccountBalanceApiCall(
                    () -> csobApiFeignClient.getAccountBalance(authorization, requestId, true, csobTppName, csobApiKey, CONTENT_TYPE, accountId),
                    accountBalanceApiMapper::toDomainBalance,
                    bankName
            );

            case Constants.KB_CLIENT_REGISTRATION_ID -> executeGetAccountBalanceApiCall(
                    () -> kbApiFeignClient.getAccountBalance(authorization, accountId),
                    accountBalanceApiMapper::toDomainBalance,
                    bankName
            );

            case Constants.REIFFEISEN_BANK_CLIENT_REGISTRATION_ID -> executeGetAccountBalanceApiCall(
                    () -> reiffeisenBankApiFeignClient.getAccountBalance(reiffeisenBankXIbmClientId, requestId, accountId),
                    accountBalanceApiMapper::toDomainBalance,
                    bankName
            );

            default ->
                    throw new IllegalArgumentException("Unsupported client registration ID: " + clientRegistrationId);
        };
    }

    private <T> Balance executeGetAccountBalanceApiCall(Supplier<ResponseEntity<T>> apiCall,
                                                        Function<T, Balance> mapper, String bankName) {
        try {
            ResponseEntity<T> response = apiCall.get();

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return mapper.apply(response.getBody());
            } else {
                throw new RuntimeException("Failed to fetch account balance from " + bankName + " API. Status: " + response.getStatusCode());
            }
        } catch (FeignException e) {
            throw new RuntimeException("Error calling " + bankName + " API: " + e.getMessage(), e);
        }
    }

    private <T> GetProductsResponse executeGetProductsApiCall(Supplier<ResponseEntity<T>> apiCall,
                                                              BiFunction<T, BankDetails, GetProductsResponse> mapper,
                                                              BankDetails bankDetails) {
        try {
            ResponseEntity<T> response = apiCall.get();

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return mapper.apply(response.getBody(), bankDetails);
            } else {
                throw new RuntimeException("Failed to fetch accounts from " + bankDetails + " API. Status: " + response.getStatusCode());
            }
        } catch (FeignException e) {
            throw new RuntimeException("Error calling " + bankDetails + " API: " + e.getMessage(), e);
        }
    }
}