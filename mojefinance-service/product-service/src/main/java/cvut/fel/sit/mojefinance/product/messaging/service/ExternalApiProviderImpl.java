package cvut.fel.sit.mojefinance.product.messaging.service;

import cvut.fel.sit.mojefinance.product.domain.dto.GetProductsResponse;
import cvut.fel.sit.mojefinance.product.messaging.client.AirBankApiFeignClient;
import cvut.fel.sit.mojefinance.product.messaging.client.CSOBApiFeignClient;
import cvut.fel.sit.mojefinance.product.messaging.client.CeskaSporitelnaApiFeignClient;
import cvut.fel.sit.mojefinance.product.messaging.client.KBApiFeignClient;
import cvut.fel.sit.mojefinance.product.messaging.client.ReiffeisenBankApiFeignClient;
import cvut.fel.sit.mojefinance.product.messaging.dto.GetProductsMessagingRequest;
import cvut.fel.sit.mojefinance.product.messaging.mapper.ProductsApiMapper;
import cvut.fel.sit.shared.util.Constants;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.function.BiFunction;
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
    public GetProductsResponse getProducts(GetProductsMessagingRequest request) {
        log.info("Fetching products for client registration ID: {}", request.getClientRegistrationId());
        String clientRegistrationId = request.getClientRegistrationId();
        String authorization = request.getAuthorization();
        String requestId = UUID.randomUUID().toString();

        return switch (clientRegistrationId) {
            case Constants.AIR_BANK_CLIENT_REGISTRATION_ID -> executeApiCall(
                    () -> airBankApiFeignClient.getAccounts(authorization),
                    productsApiMapper::toGetProductsResponse,
                    Constants.AIR_BANK_NAME
            );

            case Constants.CESKA_SPORITELNA_CLIENT_REGISTRATION_ID -> executeApiCall(
                    () -> ceskaSporitelnaApiFeignClient.getAccounts(authorization, ceskaSporitelnaApiKey),
                    productsApiMapper::toGetProductsResponse,
                    Constants.CESKA_SPORITELNA_BANK_NAME
            );

            case Constants.CSOB_CLIENT_REGISTRATION_ID -> executeApiCall(
                    () -> csobApiFeignClient.getAccounts(authorization, requestId, true, csobTppName, csobApiKey, CONTENT_TYPE),
                    productsApiMapper::toGetProductsResponse,
                    Constants.CSOB_BANK_NAME
            );

            case Constants.KB_CLIENT_REGISTRATION_ID -> executeApiCall(
                    () -> kbApiFeignClient.getAccounts(authorization),
                    productsApiMapper::toGetProductsResponse,
                    Constants.KB_BANK_NAME
            );

            case Constants.REIFFEISEN_BANK_CLIENT_REGISTRATION_ID -> executeApiCall(
                    () -> reiffeisenBankApiFeignClient.getAccounts(reiffeisenBankXIbmClientId, requestId),
                    productsApiMapper::toGetProductsResponse,
                    Constants.REIFFEISEN_BANK_NAME
            );

            default ->
                    throw new IllegalArgumentException("Unsupported client registration ID: " + clientRegistrationId);
        };
    }

    private <T> GetProductsResponse executeApiCall(Supplier<ResponseEntity<T>> apiCall,
                                                   BiFunction<T, String, GetProductsResponse> mapper,
                                                   String bankName) {
        try {
            ResponseEntity<T> response = apiCall.get();

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return mapper.apply(response.getBody(), bankName);
            } else {
                throw new RuntimeException("Failed to fetch accounts from " + bankName + " API. Status: " + response.getStatusCode());
            }
        } catch (FeignException e) {
            throw new RuntimeException("Error calling " + bankName + " API: " + e.getMessage(), e);
        }
    }
}