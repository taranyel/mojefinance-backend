package cvut.fel.sit.mojefinance.budget.domain.service;

import cvut.fel.sit.mojefinance.budget.data.entity.BudgetEntity;
import cvut.fel.sit.mojefinance.budget.data.repository.BudgetRepository;
import cvut.fel.sit.mojefinance.budget.domain.dto.BudgetRequest;
import cvut.fel.sit.mojefinance.budget.domain.dto.BudgetsResponse;
import cvut.fel.sit.mojefinance.budget.domain.entity.Budget;
import cvut.fel.sit.mojefinance.budget.domain.helper.BudgetHelper;
import cvut.fel.sit.mojefinance.budget.domain.mapper.BudgetDomainMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BudgetServiceImpl implements BudgetService {
    private final BudgetRepository budgetRepository;
    private final BudgetDomainMapper budgetDomainMapper;
    private final BudgetHelper budgetHelper;

    @Override
    public void createBudget(BudgetRequest budgetRequest) {
        budgetHelper.validateBudgetRequest(budgetRequest);

        Budget budget = budgetRequest.getBudget();
        log.info("Creating budget with category: {}, with start date: {}, for authorized user.", budget.getCategory(), budget.getStartDate());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String principalName = authentication.getName();

        boolean budgetForCategoryExists = budgetHelper.budgetForCategoryExists(principalName, budget);
        if (budgetForCategoryExists) {
            throw new IllegalArgumentException("Budget for this category already exists.");
        }
        budgetHelper.validateStartDate(budget);
        BudgetEntity newBudget = budgetDomainMapper.toBudgetEntity(budget);
        newBudget.setPrincipalName(principalName);

        budgetRepository.saveBudget(newBudget, principalName);
        log.info("Budget for category: {} created successfully.", budget.getCategory());
    }

    @Override
    public void updateBudget(BudgetRequest budgetRequest) {
        budgetHelper.validateBudgetRequest(budgetRequest);

        Budget budget = budgetRequest.getBudget();
        log.info("Updating budget with category: {} for authorized user.", budget.getCategory());

        if (budget.getBudgetId() == null) {
            throw new IllegalArgumentException("Budget ID must be provided for update.");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String principalName = authentication.getName();

        Budget existingBudget = budgetRepository.getBudgetById(budget.getBudgetId());
        budgetHelper.validateExistingBudget(existingBudget, principalName);

        budgetHelper.updateExistingBudget(existingBudget, budget);

        BudgetEntity budgetToUpdate = budgetDomainMapper.toBudgetEntity(existingBudget);
        budgetRepository.saveBudget(budgetToUpdate, principalName);
        log.info("Budget with category: {} updated successfully.", budget.getCategory());
    }

    @Override
    public BudgetsResponse getBudgets() {
        log.info("Retrieving budgets for authorized user.");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String principalName = authentication.getName();
        BudgetsResponse budgetsResponse = budgetRepository.getBudgets(principalName);
        budgetHelper.updateBudgetStartDate(budgetsResponse);
        budgetHelper.calculateSpentAmount(budgetsResponse);
        log.info("Retrieved {} budgets for user.", budgetsResponse.getBudgets().size());
        return budgetsResponse;
    }

    @Override
    public void deleteBudget(Long budgetId) {
        log.info("Deleting budget with ID: {} for authorized user.", budgetId);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String principalName = authentication.getName();
        Budget existingBudget = budgetRepository.getBudgetById(budgetId);
        budgetHelper.validateExistingBudget(existingBudget, principalName);

        budgetRepository.deleteBudget(budgetId, principalName);
        log.info("Budget with ID: {} deleted successfully.", budgetId);
    }
}
