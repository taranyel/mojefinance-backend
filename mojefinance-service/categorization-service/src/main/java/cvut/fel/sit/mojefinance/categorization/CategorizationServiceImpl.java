package cvut.fel.sit.mojefinance.categorization;

import cvut.fel.sit.mojefinance.categorization.data.entity.ProductMappingEntity;
import cvut.fel.sit.mojefinance.categorization.data.entity.TransactionMappingEntity;
import cvut.fel.sit.mojefinance.categorization.domain.dto.CategorizeProductsRequest;
import cvut.fel.sit.mojefinance.categorization.domain.dto.CategorizeProductsResponse;
import cvut.fel.sit.mojefinance.categorization.domain.dto.CategorizeTransactionsRequest;
import cvut.fel.sit.mojefinance.categorization.domain.dto.CategorizeTransactionsResponse;
import cvut.fel.sit.mojefinance.categorization.domain.helper.ProductCategorizationHelper;
import cvut.fel.sit.mojefinance.categorization.domain.helper.TransactionCategorizationHelper;
import cvut.fel.sit.shared.util.entity.ProductCategory;
import cvut.fel.sit.shared.util.entity.TransactionCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;


@Service
@RequiredArgsConstructor
public class CategorizationServiceImpl implements CategorizationService {
    private final TransactionCategorizationHelper transactionCategorizationHelper;
    private final ProductCategorizationHelper productCategorizationHelper;

    @Override
    @Cacheable(value = "categorized_transactions", key = "#categorizeTransactionsRequest.transactionNames")
    public CategorizeTransactionsResponse categorizeTransactions(CategorizeTransactionsRequest categorizeTransactionsRequest) {
        Set<String> requestedTransactionNames = categorizeTransactionsRequest.getTransactionNames();
        List<TransactionMappingEntity> transactionDataMappings = transactionCategorizationHelper
                .getAllTransactionMappingsByTransactionNames(requestedTransactionNames);

        Map<String, TransactionCategory> existingTransactionMappings = transactionCategorizationHelper
                .mapExistingTransactionMappings(transactionDataMappings);

        Set<String> unmappedTransactions = transactionCategorizationHelper
                .filterUnmappedTransactions(requestedTransactionNames, existingTransactionMappings);
        Map<String, TransactionCategory> savedTransactionMappings = transactionCategorizationHelper
                .saveNewTransactionMappings(unmappedTransactions);
        existingTransactionMappings.putAll(savedTransactionMappings);

        return CategorizeTransactionsResponse.builder()
                .categorizedTransactions(existingTransactionMappings)
                .build();
    }

    @Override
    @Cacheable(value = "categorized_products", key = "#categorizeProductsRequest.productNames")
    public CategorizeProductsResponse categorizeProducts(CategorizeProductsRequest categorizeProductsRequest) {
        Set<String> requestedProductNames = categorizeProductsRequest.getProductNames();
        List<ProductMappingEntity> productDataMappings = productCategorizationHelper
                .getAllProductMappingsByProductNames(requestedProductNames);

        Map<String, ProductCategory> existingProductMappings = productCategorizationHelper
                .mapExistingProductMappings(productDataMappings);

        Set<String> unmappedProducts = productCategorizationHelper
                .filterUnmappedProducts(requestedProductNames, existingProductMappings);
        Map<String, ProductCategory> savedTransactionMappings = productCategorizationHelper
                .saveNewProductMappings(unmappedProducts);
        existingProductMappings.putAll(savedTransactionMappings);

        return CategorizeProductsResponse.builder()
                .categorizedProducts(existingProductMappings)
                .build();
    }
}
