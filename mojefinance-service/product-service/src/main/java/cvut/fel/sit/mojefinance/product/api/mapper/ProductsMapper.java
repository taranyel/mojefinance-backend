package cvut.fel.sit.mojefinance.product.api.mapper;

import cvut.fel.sit.mojefinance.openapi.model.AssetsAndLiabilitiesResponse;
import cvut.fel.sit.mojefinance.openapi.model.Product;
import cvut.fel.sit.mojefinance.product.domain.dto.ProductsResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductsMapper {
    cvut.fel.sit.mojefinance.openapi.model.ProductsResponse toProductsResponse(ProductsResponse productsResponse);

    AssetsAndLiabilitiesResponse toAssetsAndLiabilitiesResponse(cvut.fel.sit.mojefinance.product.domain.dto.AssetsAndLiabilitiesResponse assetsAndLiabilitiesResponse);

    @Mapping(target = "productCategory", source = "productCategory.displayName")
    Product toProduct(cvut.fel.sit.mojefinance.product.domain.entity.Product product);
}
