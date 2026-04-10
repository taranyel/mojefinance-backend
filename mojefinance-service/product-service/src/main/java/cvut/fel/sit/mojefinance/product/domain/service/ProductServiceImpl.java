package cvut.fel.sit.mojefinance.product.domain.service;

import cvut.fel.sit.mojefinance.authorization.AuthorizationService;
import cvut.fel.sit.mojefinance.bank.domain.dto.ConnectedBanksDomainResponse;
import cvut.fel.sit.mojefinance.bank.domain.entity.BankConnection;
import cvut.fel.sit.mojefinance.bank.domain.service.BankConnectionService;
import cvut.fel.sit.mojefinance.categorization.CategorizationService;
import cvut.fel.sit.mojefinance.categorization.domain.dto.CategorizeProductsRequest;
import cvut.fel.sit.mojefinance.categorization.domain.dto.CategorizeProductsResponse;
import cvut.fel.sit.shared.util.entity.ProductCategory;
import cvut.fel.sit.mojefinance.product.domain.dto.AccountBalancesMessagingRequest;
import cvut.fel.sit.mojefinance.product.domain.dto.AssetsAndLiabilitiesResponse;
import cvut.fel.sit.mojefinance.product.domain.dto.ProductsResponse;
import cvut.fel.sit.mojefinance.product.domain.entity.*;
import cvut.fel.sit.mojefinance.product.domain.helper.ProductHelper;
import cvut.fel.sit.mojefinance.product.messaging.dto.ProductsMessagingRequest;
import cvut.fel.sit.mojefinance.product.messaging.service.ExternalApiProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ExternalApiProvider externalApiProvider;
    private final AuthorizationService authorizationService;
    private final BankConnectionService bankConnectionService;
    private final ProductHelper productHelper;
    private final CategorizationService categorizationService;

    @Override
    public ProductsResponse getProducts() {
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
            enrichProducts(retrievedProducts, bankDetails, authorization, principal.getName());

            products.addAll(retrievedProducts);
        }

        log.info("Retrieved {} products for authorized user.", products.size());
        return ProductsResponse.builder()
                .products(products)
                .build();
    }

    @Override
    public AssetsAndLiabilitiesResponse getAssetsAndLiabilities() {
        log.info("Getting assets and liabilities for authorized user.");
        ProductsResponse productsResponse = getProducts();
        List<GroupedProducts> assets = productHelper.groupProductsByProductType(productsResponse.getProducts(), cvut.fel.sit.shared.util.entity.ProductType.ASSET);
        List<GroupedProducts> liabilities = productHelper.groupProductsByProductType(productsResponse.getProducts(), cvut.fel.sit.shared.util.entity.ProductType.LIABILITY);
        return AssetsAndLiabilitiesResponse.builder()
                .assets(assets)
                .liabilities(liabilities)
                .build();
    }

    private List<Product> getProductsFromExternalApi(BankDetails bankDetails, String authorization, String principalName) {
        ProductsMessagingRequest productsMessagingRequest = productHelper.buildGetProductsMessagingRequest(bankDetails, authorization, principalName);
        ProductsResponse messagingResponse = externalApiProvider.getProducts(productsMessagingRequest);
        log.info("Retrieved {} products from external API for bank: {}", messagingResponse.getProducts().size(), bankDetails.getBankName());
        return messagingResponse.getProducts();
    }

    private void enrichProducts(List<Product> products, BankDetails bankDetails, String authorization, String principalName) {
        Map<String, ProductCategory> categoryMap = getProductCategoryMap(products);
        for (Product product : products) {
            setProductCategory(product, categoryMap);
            Amount balance = fetchProductBalances(product, bankDetails, authorization, principalName);
            product.setBalance(balance);
        }
    }

    private void setProductCategory(Product product, Map<String, ProductCategory> categoryMap) {
        ProductCategory category = categoryMap.get(product.getProductName());
        if (category == null) {
            category = ProductCategory.OTHER;
        }
        product.setProductCategory(category);
    }

    private Map<String, ProductCategory> getProductCategoryMap(List<Product> products) {
        CategorizeProductsRequest categorizeProductsRequest = buildCategorizeProductsRequest(products);
        CategorizeProductsResponse categorizeProductsResponse = categorizationService
                .categorizeProducts(categorizeProductsRequest);
        return categorizeProductsResponse.getCategorizedProducts();
    }

    private CategorizeProductsRequest buildCategorizeProductsRequest(List<Product> products) {
        return CategorizeProductsRequest.builder()
                .productNames(products.stream()
                        .map(Product::getProductName)
                        .collect(Collectors.toSet()))
                .build();
    }

    private Amount fetchProductBalances(Product product, BankDetails bankDetails, String authorization, String principalName) {
        String productId = product.getProductId();
        log.info("Retrieving account balance for account id: {} client registration id: {}", productId, bankDetails.getClientRegistrationId());
        AccountBalancesMessagingRequest accountBalancesMessagingRequest = productHelper.buildAccountBalancesMessagingRequest(productId, bankDetails, authorization, principalName);
        return externalApiProvider.getAccountBalance(accountBalancesMessagingRequest);
    }
}
