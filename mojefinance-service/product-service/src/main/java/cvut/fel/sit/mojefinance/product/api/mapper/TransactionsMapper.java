package cvut.fel.sit.mojefinance.product.api.mapper;

import cvut.fel.sit.mojefinance.openapi.model.Amount;
import cvut.fel.sit.mojefinance.openapi.model.TransactionsResponse;
import cvut.fel.sit.mojefinance.product.domain.dto.TransactionsDomainResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Mapper(componentModel = "spring")
public interface TransactionsMapper {
    TransactionsResponse toTransactionsResponse(TransactionsDomainResponse transactionsDomainResponse);

    @Mapping(target = "value", source = "value", qualifiedByName = "mapAmountValue")
    Amount toAmount(cvut.fel.sit.mojefinance.product.domain.entity.Amount amount);

    @Named("mapAmountValue")
    default BigDecimal mapAmountValue(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
