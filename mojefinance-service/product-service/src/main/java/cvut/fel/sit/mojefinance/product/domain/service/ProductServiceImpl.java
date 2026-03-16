package cvut.fel.sit.mojefinance.product.domain.service;

import cvut.fel.sit.mojefinance.authorization.AuthorizationService;
import cvut.fel.sit.mojefinance.bank.domain.dto.ConnectedBanksDomainResponse;
import cvut.fel.sit.mojefinance.bank.domain.entity.BankConnectionStatus;
import cvut.fel.sit.mojefinance.bank.domain.entity.BankDomainEntity;
import cvut.fel.sit.mojefinance.bank.domain.service.BankService;
import cvut.fel.sit.mojefinance.product.domain.dto.GetProductsResponse;
import cvut.fel.sit.mojefinance.product.domain.entity.Balance;
import cvut.fel.sit.mojefinance.product.domain.entity.BankDetails;
import cvut.fel.sit.mojefinance.product.domain.entity.Product;
import cvut.fel.sit.mojefinance.product.messaging.dto.AccountBalancesMessagingRequest;
import cvut.fel.sit.mojefinance.product.messaging.dto.ProductsMessagingRequest;
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
        List<Product> products = new ArrayList<>();

        ConnectedBanksDomainResponse connectedBanks = bankService.getConnectedBanks();
        List<BankDomainEntity> activeBanks = getBanksWithActiveConnection(connectedBanks);
        List<BankDomainEntity> realBanks = getRealBanks(activeBanks);

        for (BankDomainEntity realBank : realBanks) {
            String clientRegistrationId = realBank.getClientRegistrationId();
            String authorization = "Bearer " + authorizationService.authorizeClient(clientRegistrationId);
            BankDetails bankDetails = getBankDetails(realBank);

            List<Product> retrievedProducts = getProducts(bankDetails, authorization);
            getAccountBalance(retrievedProducts, bankDetails, authorization);

            products.addAll(retrievedProducts);
        }

        log.info("Retrieved {} products for authorized user.", products.size());
        return GetProductsResponse.builder()
                .products(products)
                .build();
    }

    private List<Product> getProducts(BankDetails bankDetails, String authorization) {
        ProductsMessagingRequest productsMessagingRequest = getGetProductsMessagingRequest(bankDetails, authorization);
        GetProductsResponse messagingResponse = externalApiProvider.getProducts(productsMessagingRequest);
        return messagingResponse.getProducts();
    }

    private void getAccountBalance(List<Product> retrievedProducts, BankDetails bankDetails, String authorization) {
        for (Product product : retrievedProducts) {
            String productId = product.getProductId();
            log.info("Retrieving account balance for account id: {} client registration id: {}", productId, bankDetails.getClientRegistrationId());
            AccountBalancesMessagingRequest accountBalancesMessagingRequest = getAccountBalancesMessagingRequest(productId, bankDetails, authorization);
            Balance balance = externalApiProvider.getAccountBalance(accountBalancesMessagingRequest);
            product.setBalance(balance);
        }
    }

    private AccountBalancesMessagingRequest getAccountBalancesMessagingRequest(String productId, BankDetails bankDetails, String authorization) {
        return AccountBalancesMessagingRequest.builder()
                .accountId(productId)
                .bankDetails(bankDetails)
                .authorization(authorization)
                .build();
    }

    private BankDetails getBankDetails(BankDomainEntity realBank) {
        return BankDetails.builder()
                .bankName(realBank.getBankName())
                .clientRegistrationId(realBank.getClientRegistrationId())
                .build();
    }

    private List<BankDomainEntity> getRealBanks(List<BankDomainEntity> activeBanks) {
        return activeBanks.stream()
                .filter(bank -> bank.getManuallyCreated() == false)
                .toList();
    }

    private List<BankDomainEntity> getBanksWithActiveConnection(ConnectedBanksDomainResponse connectedBanks) {
        return connectedBanks.getConnectedBanks().stream()
                .filter(connectedBank -> BankConnectionStatus.CONNECTED.equals(connectedBank.getBankConnectionStatus()))
                .toList();
    }

    private ProductsMessagingRequest getGetProductsMessagingRequest(BankDetails bankDetails, String authorization) {
        return ProductsMessagingRequest.builder()
                .authorization(authorization)
                .bankDetails(bankDetails)
                .build();
    }
}
