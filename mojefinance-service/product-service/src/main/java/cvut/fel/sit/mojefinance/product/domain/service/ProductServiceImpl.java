package cvut.fel.sit.mojefinance.product.domain.service;

import cvut.fel.sit.mojefinance.bank.domain.dto.ConnectedBanksResponse;
import cvut.fel.sit.mojefinance.bank.domain.entity.BankConnection;
import cvut.fel.sit.mojefinance.bank.domain.service.BankConnectionService;
import cvut.fel.sit.mojefinance.product.domain.dto.AssetsAndLiabilitiesResponse;
import cvut.fel.sit.mojefinance.product.domain.dto.ProductsResponse;
import cvut.fel.sit.mojefinance.product.domain.entity.AssetLiability;
import cvut.fel.sit.mojefinance.product.domain.entity.BankDetails;
import cvut.fel.sit.mojefinance.product.domain.entity.Product;
import cvut.fel.sit.mojefinance.product.domain.helper.ProductHelper;
import cvut.fel.sit.shared.entity.ProductType;
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
    private final BankConnectionService bankConnectionService;
    private final ProductHelper productHelper;

    @Override
    public ProductsResponse getProducts() {
        log.info("Getting products for authorized user.");
        List<Product> products = new ArrayList<>();

        ConnectedBanksResponse connectedBanks = bankConnectionService.getConnectedBanks();
        List<BankConnection> activeBankConnections = productHelper.filterBanksWithActiveConnection(connectedBanks);
        List<BankConnection> realBankConnections = productHelper.filterRealBanks(activeBankConnections);
        Authentication principal = SecurityContextHolder.getContext().getAuthentication();

        for (BankConnection realBankConnection : realBankConnections) {
            String clientRegistrationId = realBankConnection.getClientRegistrationId();
            String authorization = productHelper.constructAuthorizationHeader(clientRegistrationId);

            BankDetails bankDetails = productHelper.mapBankDetails(realBankConnection);
            List<Product> retrievedProducts = productHelper.getProductsFromExternalApi(bankDetails, authorization, principal.getName());
            productHelper.enrichProducts(retrievedProducts, bankDetails, authorization, principal.getName());

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
        AssetLiability assets = productHelper.buildAssetLiability(productsResponse.getProducts(), ProductType.ASSET);
        AssetLiability liabilities = productHelper.buildAssetLiability(productsResponse.getProducts(), ProductType.LIABILITY);
        return AssetsAndLiabilitiesResponse.builder()
                .assets(assets)
                .liabilities(liabilities)
                .build();
    }
}
