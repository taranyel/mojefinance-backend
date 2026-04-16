package cvut.fel.sit.mojefinance.product.domain.service;

import cvut.fel.sit.mojefinance.product.domain.dto.BudgetRequest;
import cvut.fel.sit.mojefinance.product.domain.dto.BudgetsResponse;


public interface BudgetService {
    void createBudget(BudgetRequest budgetRequest);

    void updateBudget(BudgetRequest budgetRequest);

    BudgetsResponse getBudgets();

    void deleteBudget(Long budgetId);
}
