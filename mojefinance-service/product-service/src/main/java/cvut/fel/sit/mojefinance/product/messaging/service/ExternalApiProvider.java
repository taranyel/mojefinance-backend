package cvut.fel.sit.mojefinance.product.messaging.service;

import cvut.fel.sit.mojefinance.product.domain.dto.AccountInfoRequest;
import cvut.fel.sit.mojefinance.product.domain.dto.ProductsDomainResponse;
import cvut.fel.sit.mojefinance.product.domain.entity.Amount;
import cvut.fel.sit.mojefinance.product.messaging.dto.ProductsMessagingRequest;
import cvut.fel.sit.mojefinance.product.messaging.dto.TransactionsMessagingResponse;

public interface ExternalApiProvider {
    ProductsDomainResponse getProducts(ProductsMessagingRequest request);

    Amount getAccountBalance(AccountInfoRequest request);

    TransactionsMessagingResponse getTransactions(AccountInfoRequest request);
}
