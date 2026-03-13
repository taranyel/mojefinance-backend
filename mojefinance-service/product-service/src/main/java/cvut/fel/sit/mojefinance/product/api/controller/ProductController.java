package cvut.fel.sit.mojefinance.product.api.controller;

import cvut.fel.sit.mojefinance.openapi.api.ProductsApi;
import cvut.fel.sit.mojefinance.openapi.model.ProductsResponse;
import cvut.fel.sit.mojefinance.product.api.mapper.ProductsMapper;
import cvut.fel.sit.mojefinance.product.domain.dto.GetProductsResponse;
import cvut.fel.sit.mojefinance.product.domain.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ProductController implements ProductsApi {
    private final ProductService productService;
    private final ProductsMapper productsMapper;

    @Override
    public ResponseEntity<ProductsResponse> getProducts(String authorization) {
        GetProductsResponse domainResponse = productService.getProducts();
        ProductsResponse apiResponse = productsMapper.toProductsResponse(domainResponse);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }
}
