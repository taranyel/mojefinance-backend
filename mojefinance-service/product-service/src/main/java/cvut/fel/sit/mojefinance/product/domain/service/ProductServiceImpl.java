package cvut.fel.sit.mojefinance.product.domain.service;

import cvut.fel.sit.mojefinance.authorization.AuthorizationService;
import cvut.fel.sit.mojefinance.bank.domain.dto.ConnectedBanksDomainResponse;
import cvut.fel.sit.mojefinance.bank.domain.entity.BankConnection;
import cvut.fel.sit.mojefinance.bank.domain.service.BankConnectionService;
import cvut.fel.sit.mojefinance.product.domain.dto.AccountInfoRequest;
import cvut.fel.sit.mojefinance.product.domain.dto.ProductsDomainResponse;
import cvut.fel.sit.mojefinance.product.domain.entity.Amount;
import cvut.fel.sit.mojefinance.product.domain.entity.BankDetails;
import cvut.fel.sit.mojefinance.product.domain.entity.Product;
import cvut.fel.sit.mojefinance.product.domain.helper.ProductHelper;import cvut.fel.sit.mojefinance.product.messaging.dto.ProductsMessagingRequest;
import cvut.fel.sit.mojefinance.product.messaging.service.ExternalApiProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ExternalApiProvider externalApiProvider;
    private final AuthorizationService authorizationService;
    private final BankConnectionService bankConnectionService;
    private final ProductHelper productHelper;

    @Override
    public ProductsDomainResponse getProducts() {
        log.info("Getting products for authorized user.");
        List<Product> products = new ArrayList<>();

        ConnectedBanksDomainResponse connectedBanks = bankConnectionService.getConnectedBanks();
        List<BankConnection> activeBankConnections = productHelper.filterBanksWithActiveConnection(connectedBanks);
        List<BankConnection> realBankConnections = productHelper.filterRealBanks(activeBankConnections);
        Authentication principal = SecurityContextHolder.getContext().getAuthentication();

        for (BankConnection realBankConnection : realBankConnections) {
            String clientRegistrationId = realBankConnection.getClientRegistrationId();
            String authorization = "Bearer " + authorizationService.authorizeClient(clientRegistrationId);

            BankDetails bankDetails = productHelper.mapBankDetails(realBankConnection);
            List<Product> retrievedProducts = getProductsFromExternalApi(bankDetails, authorization, principal.getName());
            setProductsBalances(retrievedProducts, bankDetails, authorization, principal.getName());

            products.addAll(retrievedProducts);
        }

        log.info("Retrieved {} products for authorized user.", products.size());
        return ProductsDomainResponse.builder()
                .products(products)
                .build();
    }

    private List<Product> getProductsFromExternalApi(BankDetails bankDetails, String authorization, String principalName) {
        ProductsMessagingRequest productsMessagingRequest = productHelper.buildGetProductsMessagingRequest(bankDetails, authorization, principalName);
        ProductsDomainResponse messagingResponse = externalApiProvider.getProducts(productsMessagingRequest);
        return messagingResponse.getProducts();
    }

    private void setProductsBalances(List<Product> retrievedProducts, BankDetails bankDetails, String authorization, String principalName) {
        for (Product product : retrievedProducts) {
            String productId = product.getProductId();
            log.info("Retrieving account balance for account id: {} client registration id: {}", productId, bankDetails.getClientRegistrationId());
            AccountInfoRequest accountInfoRequest = productHelper.buildAccountBalancesMessagingRequest(productId, bankDetails, authorization, principalName);
            Amount balance = externalApiProvider.getAccountBalance(accountInfoRequest);
            product.setBalance(balance);
        }
    }
}
