package cvut.fel.sit.mojefinance.product.domain.helper;

import cvut.fel.sit.mojefinance.authorization.AuthorizationService;
import cvut.fel.sit.mojefinance.bank.domain.dto.ConnectedBanksResponse;
import cvut.fel.sit.mojefinance.bank.domain.entity.BankConnection;
import cvut.fel.sit.mojefinance.bank.domain.entity.BankConnectionStatus;
import cvut.fel.sit.mojefinance.categorization.CategorizationService;
import cvut.fel.sit.mojefinance.categorization.domain.dto.CategorizeProductsRequest;
import cvut.fel.sit.mojefinance.categorization.domain.dto.CategorizeProductsResponse;
import cvut.fel.sit.mojefinance.product.domain.dto.AccountBalancesMessagingRequest;
import cvut.fel.sit.mojefinance.product.domain.dto.ProductsResponse;
import cvut.fel.sit.mojefinance.product.domain.entity.*;
import cvut.fel.sit.mojefinance.product.messaging.dto.ProductsMessagingRequest;
import cvut.fel.sit.mojefinance.product.messaging.service.ExternalApiProvider;
import cvut.fel.sit.shared.entity.ProductCategory;
import cvut.fel.sit.shared.entity.ProductType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cvut.fel.sit.shared.util.Constants.CZK_CURRENCY_CODE;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductHelper {
    private final ExternalApiProvider externalApiProvider;
    private final CategorizationService categorizationService;
    private final AuthorizationService authorizationService;
    private static final String BEARER_PREFIX = "Bearer";

    public AccountBalancesMessagingRequest buildAccountBalancesMessagingRequest(String productId, BankDetails bankDetails, String authorization, String principalName) {
        return AccountBalancesMessagingRequest.builder()
                .accountId(productId)
                .bankDetails(bankDetails)
                .authorization(authorization)
                .principalName(principalName)
                .build();
    }

    public BankDetails mapBankDetails(BankConnection realBankConnection) {
        return BankDetails.builder()
                .bankName(realBankConnection.getBankName())
                .clientRegistrationId(realBankConnection.getClientRegistrationId())
                .build();
    }

    public List<BankConnection> filterRealBanks(List<BankConnection> activeBankConnections) {
        return activeBankConnections.stream()
                .filter(bank -> bank.getManuallyCreated() == false)
                .toList();
    }

    public List<BankConnection> filterBanksWithActiveConnection(ConnectedBanksResponse connectedBanks) {
        return connectedBanks.getConnectedBanks().stream()
                .filter(connectedBank -> BankConnectionStatus.CONNECTED.equals(connectedBank.getBankConnectionStatus()))
                .toList();
    }

    public AssetLiability buildAssetLiability(List<Product> products, ProductType productType) {
        List<GroupedProducts> groupedProducts = groupProductsByProductType(products, productType);
        Amount totalAmount = buildTotalAmount(groupedProducts.stream()
                .flatMap(group -> group.getProducts().stream())
                .toList());
        return AssetLiability.builder()
                .groupedProducts(groupedProducts)
                .totalAmount(totalAmount)
                .build();
    }

    public List<Product> getProductsFromExternalApi(BankDetails bankDetails, String authorization, String principalName) {
        ProductsMessagingRequest productsMessagingRequest = buildGetProductsMessagingRequest(bankDetails, authorization, principalName);
        ProductsResponse messagingResponse = externalApiProvider.getProducts(productsMessagingRequest);
        log.info("Retrieved {} products from external API for bank: {}", messagingResponse.getProducts().size(), bankDetails.getBankName());
        return messagingResponse.getProducts();
    }

    public void enrichProducts(List<Product> products, BankDetails bankDetails, String authorization, String principalName) {
        Map<String, ProductCategory> categoryMap = getProductCategoryMap(products);
        for (Product product : products) {
            setProductCategory(product, categoryMap);
            Amount balance = fetchProductBalances(product, bankDetails, authorization, principalName);
            product.setBalance(balance);
        }
    }

    public String constructAuthorizationHeader(String clientRegistrationId) {
        return BEARER_PREFIX + " " + authorizationService.authorizeClient(clientRegistrationId);
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
        AccountBalancesMessagingRequest accountBalancesMessagingRequest = buildAccountBalancesMessagingRequest(productId, bankDetails, authorization, principalName);
        return externalApiProvider.getAccountBalance(accountBalancesMessagingRequest);
    }

    private ProductsMessagingRequest buildGetProductsMessagingRequest(BankDetails bankDetails, String authorization, String principalName) {
        return ProductsMessagingRequest.builder()
                .authorization(authorization)
                .bankDetails(bankDetails)
                .principalName(principalName)
                .build();
    }

    private List<GroupedProducts> groupProductsByProductType(List<Product> products, ProductType productType) {
        return products.stream()
                .filter(product -> product.getProductCategory().getProductType() == productType)
                .collect(Collectors.groupingBy(Product::getProductCategory))
                .entrySet().stream()
                .map(entry -> GroupedProducts.builder()
                        .groupName(entry.getKey().getDisplayName())
                        .products(entry.getValue())
                        .totalAmount(buildTotalAmount(entry.getValue()))
                        .build())
                .sorted(Comparator.comparing(groupedProducts -> groupedProducts.getTotalAmount().getValue(), Comparator.reverseOrder()))
                .toList();
    }

    private Amount buildTotalAmount(List<Product> products) {
        BigDecimal totalAmount = products.stream()
                .map(Product::getBalance)
                .map(Amount::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return Amount.builder()
                .value(totalAmount)
                .currency(CZK_CURRENCY_CODE)
                .build();
    }
}
