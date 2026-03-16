package cvut.fel.sit.mojefinance.product.api.mapper;

import cvut.fel.sit.mojefinance.openapi.model.ProductsResponse;
import cvut.fel.sit.mojefinance.openapi.model.TransactionsResponse;import cvut.fel.sit.mojefinance.product.domain.dto.ProductsDomainResponse;
import cvut.fel.sit.mojefinance.product.domain.dto.TransactionsDomainResponse;import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductsMapper {

    ProductsResponse toProductsResponse(ProductsDomainResponse productsDomainResponse);

    TransactionsResponse toTransactionsResponse(TransactionsDomainResponse transactionsDomainResponse);
}
