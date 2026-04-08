package cvut.fel.sit.mojefinance.product.api.mapper;

import cvut.fel.sit.mojefinance.product.domain.dto.ProductsResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductsMapper {
    cvut.fel.sit.mojefinance.openapi.model.ProductsResponse toProductsResponse(ProductsResponse productsResponse);
}
