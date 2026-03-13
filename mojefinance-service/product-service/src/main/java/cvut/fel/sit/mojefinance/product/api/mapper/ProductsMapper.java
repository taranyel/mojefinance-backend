package cvut.fel.sit.mojefinance.product.api.mapper;

import cvut.fel.sit.mojefinance.openapi.model.ProductsResponse;
import cvut.fel.sit.mojefinance.product.domain.dto.GetProductsResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductsMapper {

    ProductsResponse toProductsResponse(GetProductsResponse getProductsResponse);
}
