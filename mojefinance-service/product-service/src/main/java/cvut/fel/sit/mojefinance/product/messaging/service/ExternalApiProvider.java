package cvut.fel.sit.mojefinance.product.messaging.service;

import cvut.fel.sit.mojefinance.product.domain.dto.GetProductsResponse;
import cvut.fel.sit.mojefinance.product.messaging.dto.GetProductsMessagingRequest;

public interface ExternalApiProvider {
    GetProductsResponse getProducts(GetProductsMessagingRequest request);
}
