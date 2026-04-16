package cvut.fel.sit.mojefinance.product.domain.service;

import cvut.fel.sit.mojefinance.product.data.entity.BudgetEntity;
import cvut.fel.sit.mojefinance.product.data.repository.BudgetRepository;
import cvut.fel.sit.mojefinance.product.domain.dto.BudgetRequest;
import cvut.fel.sit.mojefinance.product.domain.dto.BudgetsResponse;
import cvut.fel.sit.mojefinance.product.domain.dto.TransactionsDomainResponse;
import cvut.fel.sit.mojefinance.product.domain.entity.Amount;
import cvut.fel.sit.mojefinance.product.domain.entity.Budget;
import cvut.fel.sit.mojefinance.product.domain.entity.BudgetStatus;
import cvut.fel.sit.mojefinance.product.domain.entity.GroupedTransactions;
import cvut.fel.sit.mojefinance.product.domain.mapper.BudgetDomainMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BudgetServiceImpl implements BudgetService {
    private final BudgetRepository budgetRepository;
    private final BudgetDomainMapper budgetDomainMapper;
    private final TransactionService transactionService;

    @Override
    public void createBudget(BudgetRequest budgetRequest) {
        validateBudgetRequest(budgetRequest);

        Budget budget = budgetRequest.getBudget();
        log.info("Creating budget with category: {}, with start date: {}, for authorized user.", budget.getCategory(), budget.getStartDate());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String principalName = authentication.getName();

        boolean budgetForCategoryExists = budgetForCategoryExists(principalName, budget);
        if (budgetForCategoryExists) {
            throw new IllegalArgumentException("Budget for this category already exists.");
        }
        validateStartDate(budget);
        budget.setBudgetStatus(BudgetStatus.ACTIVE);
        BudgetEntity newBudget = budgetDomainMapper.toBudgetEntity(budget);
        newBudget.setPrincipalName(principalName);

        budgetRepository.saveBudget(newBudget);
        log.info("Budget for category: {} created successfully.", budget.getCategory());
    }

    @Override
    public void updateBudget(BudgetRequest budgetRequest) {
        validateBudgetRequest(budgetRequest);

        Budget budget = budgetRequest.getBudget();
        log.info("Updating budget with category: {} for authorized user.", budget.getCategory());

        if (budget.getBudgetId() == null) {
            throw new IllegalArgumentException("Budget ID must be provided for update.");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String principalName = authentication.getName();

        Budget existingBudget = budgetRepository.getBudgetById(budget.getBudgetId());
        validateExistingBudget(existingBudget, principalName);

        updateExistingBudget(existingBudget, budget);

        BudgetEntity budgetToUpdate = budgetDomainMapper.toBudgetEntity(existingBudget);
        budgetRepository.saveBudget(budgetToUpdate);
        log.info("Budget with category: {} updated successfully.", budget.getCategory());
    }

    @Override
    public BudgetsResponse getBudgets() {
        log.info("Retrieving budgets for authorized user.");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String principalName = authentication.getName();
        BudgetsResponse budgetsResponse = budgetRepository.getBudgets(principalName);
        updateBudgetStartDate(budgetsResponse);
        calculateSpentAmount(budgetsResponse);
        log.info("Retrieved {} budgets for user.", budgetsResponse.getBudgets().size());
        return budgetsResponse;
    }

    @Override
    public void deleteBudget(Long budgetId) {
        log.info("Deleting budget with ID: {} for authorized user.", budgetId);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String principalName = authentication.getName();
        Budget existingBudget = budgetRepository.getBudgetById(budgetId);
        validateExistingBudget(existingBudget, principalName);

        budgetRepository.deleteBudget(budgetId);
        log.info("Budget with ID: {} deleted successfully.", budgetId);
    }

    private void validateStartDate(Budget budget) {
        if (budget.getStartDate() == null) {
            LocalDate firstDayOfCurrentMonth = LocalDate.now().withDayOfMonth(1);
            budget.setStartDate(firstDayOfCurrentMonth);
            log.info("Start date not provided. Setting start date to first day of current month: {}", firstDayOfCurrentMonth);
        }
    }

    private void updateBudgetStartDate(BudgetsResponse budgetsResponse) {
        for (Budget budget : budgetsResponse.getBudgets()) {
            YearMonth nextMonth = YearMonth.from(budget.getStartDate()).plusMonths(1);
            MonthDay monthDay = MonthDay.from(budget.getStartDate());
            LocalDate endDate = LocalDate.of(nextMonth.getYear(), nextMonth.getMonth(), monthDay.getDayOfMonth());

            if (LocalDate.now().equals(endDate)) {
                log.info("Budget restarted for category: {}. Old start date: {}, New start date: {}", budget.getCategory(), budget.getStartDate(), endDate);
                budget.setStartDate(endDate);
                budget.setBudgetStatus(BudgetStatus.ACTIVE);
            }
        }
    }

    private void calculateSpentAmount(BudgetsResponse budgetsResponse) {
        Map<LocalDate, List<Budget>> budgetsGroupedByStartDate = budgetsResponse.getBudgets().stream()
                .collect(Collectors.groupingBy(Budget::getStartDate));

        for (Map.Entry<LocalDate, List<Budget>> entry : budgetsGroupedByStartDate.entrySet()) {
            LocalDate startDate = entry.getKey();
            List<Budget> budgetsForDate = entry.getValue();
            log.info("Processing budgets with start date: {}. Number of budgets: {}", startDate, budgetsForDate.size());

            TransactionsDomainResponse transactionsResponse = transactionService.getCashFlowSummary(startDate);
            Map<String, BigDecimal> spentAmountPerCategory = getSpentAmountPerCategory(transactionsResponse);
            enrichBudgets(budgetsForDate, spentAmountPerCategory);
        }
    }

    private void enrichBudgets(List<Budget> budgetsForDate, Map<String, BigDecimal> spentAmountPerCategory) {
        budgetsForDate.forEach(budget -> {
            BigDecimal spentAmount = spentAmountPerCategory.getOrDefault(budget.getCategory().getDisplayName(), BigDecimal.ZERO);
            Amount spentAmountObj = Amount.builder()
                    .value(spentAmount)
                    .currency(budget.getAmount().getCurrency())
                    .build();
            budget.setSpentAmount(spentAmountObj);

            if (spentAmount.compareTo(budget.getAmount().getValue()) >= 0) {
                budget.setBudgetStatus(BudgetStatus.EXCEEDED);
                log.info("Budget exceeded for category: {}. Spent amount: {}, Budget amount: {}", budget.getCategory(), spentAmount, budget.getAmount().getValue());
            }

            log.info("Budget category: {}, Spent amount: {}", budget.getCategory(), spentAmount);
        });
    }

    private Map<String, BigDecimal> getSpentAmountPerCategory(TransactionsDomainResponse transactionsResponse) {
        return transactionsResponse.getGroupedTransactions().stream()
                .flatMap(groupedByMonth -> groupedByMonth.getGroupedTransactions().stream())
                .collect(Collectors.groupingBy(GroupedTransactions::getGroupName,
                        Collectors.mapping(groupedByCategory -> groupedByCategory.getTotalExpense().getValue(),
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));
    }

    private boolean budgetForCategoryExists(String principalName, Budget budget) {
        BudgetsResponse existingBudgets = budgetRepository.getBudgets(principalName);
        return existingBudgets.getBudgets().stream()
                .anyMatch(b -> b.getCategory() == budget.getCategory());
    }

    private void validateBudgetRequest(BudgetRequest budgetRequest) {
        if (budgetRequest == null || budgetRequest.getBudget() == null) {
            throw new IllegalArgumentException("BudgetRequest and its Budget cannot be null.");
        }
    }

    private void updateExistingBudget(Budget existingBudget, Budget newBudget) {
        existingBudget.setAmount(newBudget.getAmount());
        existingBudget.setCategory(newBudget.getCategory());
        existingBudget.setStartDate(newBudget.getStartDate());
    }

    private void validateExistingBudget(Budget existingBudget, String principalName) {
        if (existingBudget == null) {
            throw new IllegalArgumentException("Budget not found.");
        }
        if (!existingBudget.getPrincipalName().equals(principalName)) {
            throw new SecurityException("Unauthorized to update this budget.");
        }
    }
}
