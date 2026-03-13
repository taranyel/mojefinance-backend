package cvut.fel.sit.mojefinance.product.domain.service;

import cvut.fel.sit.mojefinance.authorization.AuthorizationService;
import cvut.fel.sit.mojefinance.bank.domain.dto.ConnectedBanksDomainResponse;
import cvut.fel.sit.mojefinance.bank.domain.entity.BankDomainEntity;
import cvut.fel.sit.mojefinance.bank.domain.service.BankService;
import cvut.fel.sit.mojefinance.product.domain.dto.GetProductsResponse;
import cvut.fel.sit.mojefinance.product.domain.entity.Product;
import cvut.fel.sit.mojefinance.product.messaging.dto.GetProductsMessagingRequest;
import cvut.fel.sit.mojefinance.product.messaging.service.ExternalApiProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ExternalApiProvider externalApiProvider;
    private final AuthorizationService authorizationService;
    private final BankService bankService;

    @Override
    public GetProductsResponse getProducts() {
        log.info("Getting products for authorized user.");

        ConnectedBanksDomainResponse connectedBanks = bankService.getConnectedBanks();
        List<Product> products = new ArrayList<>();

        for (BankDomainEntity bank : connectedBanks.getConnectedBanks()) {
            String clientRegistrationId = bank.getClientRegistrationId();
            String authorization = "Bearer " + authorizationService.authorizeClient(clientRegistrationId);
            GetProductsMessagingRequest messagingRequest = getGetProductsMessagingRequest(clientRegistrationId, authorization);

            GetProductsResponse messagingResponse = externalApiProvider.getProducts(messagingRequest);
            products.addAll(messagingResponse.getProducts());
        }
        log.info("Retrieved {} products for authorized user.", products.size());
        return GetProductsResponse.builder()
                .products(products)
                .build();
    }

    private GetProductsMessagingRequest getGetProductsMessagingRequest(String clientRegistrationId, String authorization) {
        return GetProductsMessagingRequest.builder()
                .authorization(authorization)
                .clientRegistrationId(clientRegistrationId)
                .build();
    }
}
