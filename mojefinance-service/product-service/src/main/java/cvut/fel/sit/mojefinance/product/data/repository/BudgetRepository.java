package cvut.fel.sit.mojefinance.product.data.repository;

import cvut.fel.sit.mojefinance.product.data.entity.BudgetEntity;
import cvut.fel.sit.mojefinance.product.domain.dto.BudgetsResponse;
import cvut.fel.sit.mojefinance.product.domain.entity.Budget;

public interface BudgetRepository {
    void saveBudget(BudgetEntity budgetEntity);

    BudgetsResponse getBudgets(String principalName);

    void deleteBudget(Long budgetId);

    Budget getBudgetById(Long budgetId);
}
