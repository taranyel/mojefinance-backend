package cvut.fel.sit.mojefinance.categorization;

import cvut.fel.sit.mojefinance.categorization.domain.dto.CategorizeProductsRequest;
import cvut.fel.sit.mojefinance.categorization.domain.dto.CategorizeProductsResponse;
import cvut.fel.sit.mojefinance.categorization.domain.dto.CategorizeTransactionsRequest;
import cvut.fel.sit.mojefinance.categorization.domain.dto.CategorizeTransactionsResponse;


public interface CategorizationService {
    CategorizeTransactionsResponse categorizeTransactions(CategorizeTransactionsRequest categorizeTransactionsRequest);

    CategorizeProductsResponse categorizeProducts(CategorizeProductsRequest categorizeProductsRequest);
}
