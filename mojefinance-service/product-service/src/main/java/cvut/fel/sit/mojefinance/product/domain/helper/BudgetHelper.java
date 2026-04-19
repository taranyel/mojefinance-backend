package cvut.fel.sit.mojefinance.product.domain.helper;

import cvut.fel.sit.mojefinance.product.data.repository.BudgetRepository;
import cvut.fel.sit.mojefinance.product.domain.dto.BudgetRequest;
import cvut.fel.sit.mojefinance.product.domain.dto.BudgetsResponse;
import cvut.fel.sit.mojefinance.product.domain.dto.TransactionsDomainResponse;
import cvut.fel.sit.mojefinance.product.domain.entity.Amount;
import cvut.fel.sit.mojefinance.product.domain.entity.Budget;
import cvut.fel.sit.mojefinance.product.domain.entity.BudgetStatus;
import cvut.fel.sit.mojefinance.product.domain.entity.GroupedTransactions;
import cvut.fel.sit.mojefinance.product.domain.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class BudgetHelper {
    private final TransactionService transactionService;
    private final BudgetRepository budgetRepository;

    public void validateStartDate(Budget budget) {
        if (budget.getStartDate() == null) {
            LocalDate firstDayOfCurrentMonth = LocalDate.now().withDayOfMonth(1);
            budget.setStartDate(firstDayOfCurrentMonth);
            log.info("Start date not provided. Setting start date to first day of current month: {}", firstDayOfCurrentMonth);
        }
    }

    public void updateBudgetStartDate(BudgetsResponse budgetsResponse) {
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

    public void calculateSpentAmount(BudgetsResponse budgetsResponse) {
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

    public boolean budgetForCategoryExists(String principalName, Budget budget) {
        BudgetsResponse existingBudgets = budgetRepository.getBudgets(principalName);
        return existingBudgets.getBudgets().stream()
                .anyMatch(b -> b.getCategory() == budget.getCategory());
    }

    public void validateBudgetRequest(BudgetRequest budgetRequest) {
        if (budgetRequest == null || budgetRequest.getBudget() == null) {
            throw new IllegalArgumentException("BudgetRequest and its Budget cannot be null.");
        }
    }

    public void updateExistingBudget(Budget existingBudget, Budget newBudget) {
        existingBudget.setAmount(newBudget.getAmount());
        existingBudget.setCategory(newBudget.getCategory());
        existingBudget.setStartDate(newBudget.getStartDate());
    }

    public void validateExistingBudget(Budget existingBudget, String principalName) {
        if (existingBudget == null) {
            throw new IllegalArgumentException("Budget not found.");
        }
        if (!existingBudget.getPrincipalName().equals(principalName)) {
            throw new SecurityException("Unauthorized to update this budget.");
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
                        Collectors.mapping(groupedByCategory -> groupedByCategory.getTotalExpense().getValue().negate(),
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));
    }
}
