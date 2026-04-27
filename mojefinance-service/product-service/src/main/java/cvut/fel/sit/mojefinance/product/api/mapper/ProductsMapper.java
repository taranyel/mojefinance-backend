package cvut.fel.sit.mojefinance.product.api.mapper;

import cvut.fel.sit.mojefinance.openapi.model.Amount;
import cvut.fel.sit.mojefinance.openapi.model.AssetsAndLiabilitiesResponse;
import cvut.fel.sit.mojefinance.openapi.model.Product;
import cvut.fel.sit.mojefinance.product.domain.dto.ProductsResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Mapper(componentModel = "spring")
public interface ProductsMapper {
    cvut.fel.sit.mojefinance.openapi.model.ProductsResponse toProductsResponse(ProductsResponse productsResponse);

    AssetsAndLiabilitiesResponse toAssetsAndLiabilitiesResponse(cvut.fel.sit.mojefinance.product.domain.dto.AssetsAndLiabilitiesResponse assetsAndLiabilitiesResponse);

    @Mapping(target = "productCategory", source = "productCategory.displayName")
    Product toProduct(cvut.fel.sit.mojefinance.product.domain.entity.Product product);

    @Mapping(target = "value", source = "value", qualifiedByName = "mapAmountValue")
    Amount toAmount(cvut.fel.sit.mojefinance.product.domain.entity.Amount amount);

    @Named("mapAmountValue")
    default BigDecimal mapAmountValue(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
