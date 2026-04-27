package cvut.fel.sit.mojefinance.budget.api.mapper;

import cvut.fel.sit.mojefinance.budget.domain.dto.BudgetsResponse;
import cvut.fel.sit.mojefinance.budget.domain.dto.BudgetRequest;
import cvut.fel.sit.mojefinance.openapi.model.Amount;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Mapper(componentModel = "spring")
public interface BudgetsMapper {
    BudgetRequest toBudgetRequest(cvut.fel.sit.mojefinance.openapi.model.BudgetRequest budgetRequest);

    cvut.fel.sit.mojefinance.openapi.model.BudgetsResponse toBudgetsResponse(BudgetsResponse budgetsResponse);

    @Mapping(target = "value", source = "value", qualifiedByName = "mapAmountValue")
    Amount toAmount(cvut.fel.sit.mojefinance.product.domain.entity.Amount amount);

    @Named("mapAmountValue")
    default BigDecimal mapAmountValue(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
