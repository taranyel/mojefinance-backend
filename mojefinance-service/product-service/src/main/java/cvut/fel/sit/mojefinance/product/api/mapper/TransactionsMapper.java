package cvut.fel.sit.mojefinance.product.api.mapper;

import cvut.fel.sit.mojefinance.openapi.model.Transaction;
import cvut.fel.sit.mojefinance.openapi.model.TransactionsGroupedByCategory;
import cvut.fel.sit.mojefinance.openapi.model.TransactionsResponse;
import cvut.fel.sit.mojefinance.product.domain.dto.TransactionsDomainResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionsMapper {
    TransactionsResponse toTransactionsResponse(TransactionsDomainResponse transactionsDomainResponse);

    @Mapping(target = "groupName", source = "groupName.displayName")
    TransactionsGroupedByCategory toTransactionsGroupedByCategory(cvut.fel.sit.mojefinance.product.domain.entity.TransactionsGroupedByCategory transactionsGroupedByCategory);

    @Mapping(target = "category", source = "category.displayName")
    Transaction toTransaction(cvut.fel.sit.mojefinance.product.domain.entity.Transaction transaction);
}
