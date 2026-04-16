package cvut.fel.sit.mojefinance.product.api.mapper;

import cvut.fel.sit.mojefinance.openapi.model.BudgetsResponse;
import cvut.fel.sit.mojefinance.product.domain.dto.BudgetRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BudgetsMapper {
    BudgetRequest toBudgetRequest(cvut.fel.sit.mojefinance.openapi.model.BudgetRequest budgetRequest);

    BudgetsResponse toBudgetsResponse(cvut.fel.sit.mojefinance.product.domain.dto.BudgetsResponse budgetsResponse);
}
