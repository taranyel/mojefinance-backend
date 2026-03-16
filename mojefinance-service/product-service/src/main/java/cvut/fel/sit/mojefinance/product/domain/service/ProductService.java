package cvut.fel.sit.mojefinance.product.domain.service;

import cvut.fel.sit.mojefinance.product.domain.dto.ProductsDomainResponse;
import cvut.fel.sit.mojefinance.product.domain.dto.TransactionsDomainResponse;import cvut.fel.sit.mojefinance.product.domain.dto.AccountInfoRequest;

public interface ProductService {
    ProductsDomainResponse getProducts();

    TransactionsDomainResponse getTransactions(AccountInfoRequest request);
}
