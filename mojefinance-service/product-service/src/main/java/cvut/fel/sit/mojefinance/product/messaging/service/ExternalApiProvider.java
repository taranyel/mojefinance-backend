package cvut.fel.sit.mojefinance.product.messaging.service;

import cvut.fel.sit.mojefinance.product.domain.dto.GetProductsResponse;
import cvut.fel.sit.mojefinance.product.domain.entity.Balance;
import cvut.fel.sit.mojefinance.product.messaging.dto.AccountBalancesMessagingRequest;
import cvut.fel.sit.mojefinance.product.messaging.dto.ProductsMessagingRequest;

public interface ExternalApiProvider {
    GetProductsResponse getProducts(ProductsMessagingRequest request);

    Balance getAccountBalance(AccountBalancesMessagingRequest request);
}
