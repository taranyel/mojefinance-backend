package cvut.fel.sit.mojefinance.budget.api.mapper;

import cvut.fel.sit.mojefinance.budget.domain.dto.BudgetsResponse;
import cvut.fel.sit.mojefinance.budget.domain.dto.BudgetRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BudgetsMapper {
    BudgetRequest toBudgetRequest(cvut.fel.sit.mojefinance.openapi.model.BudgetRequest budgetRequest);

    cvut.fel.sit.mojefinance.openapi.model.BudgetsResponse toBudgetsResponse(BudgetsResponse budgetsResponse);
}
