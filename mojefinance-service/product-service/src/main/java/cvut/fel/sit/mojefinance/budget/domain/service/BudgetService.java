package cvut.fel.sit.mojefinance.budget.domain.service;

import cvut.fel.sit.mojefinance.budget.domain.dto.BudgetRequest;
import cvut.fel.sit.mojefinance.budget.domain.dto.BudgetsResponse;


public interface BudgetService {
    void createBudget(BudgetRequest budgetRequest);

    void updateBudget(BudgetRequest budgetRequest);

    BudgetsResponse getBudgets();

    void deleteBudget(Long budgetId);
}
