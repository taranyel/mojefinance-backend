package cvut.fel.sit.mojefinance.budget.data.repository;

import cvut.fel.sit.mojefinance.budget.data.entity.BudgetEntity;
import cvut.fel.sit.mojefinance.budget.domain.dto.BudgetsResponse;
import cvut.fel.sit.mojefinance.budget.domain.entity.Budget;

public interface BudgetRepository {
    void saveBudget(BudgetEntity budgetEntity, String principalName);

    BudgetsResponse getBudgets(String principalName);

    void deleteBudget(Long budgetId, String principalName);

    Budget getBudgetById(Long budgetId);
}
