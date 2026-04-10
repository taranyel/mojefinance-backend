package cvut.fel.sit.mojefinance.product.domain.service;

import cvut.fel.sit.mojefinance.product.domain.dto.AssetsAndLiabilitiesResponse;
import cvut.fel.sit.mojefinance.product.domain.dto.ProductsResponse;

public interface ProductService {
    ProductsResponse getProducts();

    AssetsAndLiabilitiesResponse getAssetsAndLiabilities();
}
