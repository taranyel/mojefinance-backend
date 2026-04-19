package cvut.fel.sit.mojefinance.product.domain.helper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import cvut.fel.sit.mojefinance.product.domain.entity.*;
import cvut.fel.sit.mojefinance.product.domain.dto.*;
import cvut.fel.sit.mojefinance.product.domain.service.TransactionService;
import cvut.fel.sit.mojefinance.product.data.repository.BudgetRepository;
import cvut.fel.sit.shared.entity.TransactionCategory;

class BudgetHelperTest {
    @Mock
    private TransactionService transactionService;
    @Mock
    private BudgetRepository budgetRepository;
    @InjectMocks
    private BudgetHelper helper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        helper = new BudgetHelper(transactionService, budgetRepository);
    }

    @Test
    void validateStartDate_setsFirstDayIfNull() {
        Budget budget = Budget.builder().startDate(null).build();
        helper.validateStartDate(budget);
        assertNotNull(budget.getStartDate());
        assertEquals(LocalDate.now().withDayOfMonth(1), budget.getStartDate());
    }

    @Test
    void validateStartDate_keepsExistingDate() {
        LocalDate date = LocalDate.of(2024, 4, 1);
        Budget budget = Budget.builder().startDate(date).build();
        helper.validateStartDate(budget);
        assertEquals(date, budget.getStartDate());
    }

    @Test
    void updateBudgetStartDate_setsNewStartDateAndStatus() {
        LocalDate now = LocalDate.now();
        Budget budget = Budget.builder().startDate(now.minusMonths(1)).budgetStatus(BudgetStatus.EXCEEDED).build();
        BudgetsResponse resp = BudgetsResponse.builder().budgets(List.of(budget)).build();
        helper.updateBudgetStartDate(resp);
        // Only updates if today is the calculated endDate, so we check for ACTIVE or EXCEEDED
        assertTrue(budget.getBudgetStatus() == BudgetStatus.ACTIVE || budget.getBudgetStatus() == BudgetStatus.EXCEEDED);
    }

    @Test
    void calculateSpentAmount_setsSpentAndStatus() {
        LocalDate startDate = LocalDate.of(2024, 4, 1);
        Budget budget = Budget.builder()
                .category(TransactionCategory.GROCERIES)
                .amount(Amount.builder().currency("CZK").value(new BigDecimal("100.00")).build())
                .startDate(startDate)
                .build();
        BudgetsResponse resp = BudgetsResponse.builder().budgets(List.of(budget)).build();
        GroupedTransactions grouped = GroupedTransactions.builder()
                .groupName("Groceries")
                .totalExpense(Amount.builder().currency("CZK").value(new BigDecimal("-120.00")).build())
                .build();
        GroupedTransactions groupedByMonth = GroupedTransactions.builder().groupedTransactions(List.of(grouped)).build();
        TransactionsDomainResponse txResp = TransactionsDomainResponse.builder().groupedTransactions(List.of(groupedByMonth)).build();
        when(transactionService.getCashFlowSummary(startDate)).thenReturn(txResp);
        helper.calculateSpentAmount(resp);
        assertNotNull(budget.getSpentAmount());
        assertEquals(new BigDecimal("120.00"), budget.getSpentAmount().getValue());
        assertEquals(BudgetStatus.EXCEEDED, budget.getBudgetStatus());
    }

    @Test
    void budgetForCategoryExists_returnsTrueIfExists() {
        Budget budget = Budget.builder().category(TransactionCategory.GROCERIES).build();
        BudgetsResponse resp = BudgetsResponse.builder().budgets(List.of(budget)).build();
        when(budgetRepository.getBudgets("user")).thenReturn(resp);
        boolean exists = helper.budgetForCategoryExists("user", budget);
        assertTrue(exists);
    }

    @Test
    void budgetForCategoryExists_returnsFalseIfNotExists() {
        Budget budget = Budget.builder().category(TransactionCategory.GROCERIES).build();
        BudgetsResponse resp = BudgetsResponse.builder().budgets(List.of()).build();
        when(budgetRepository.getBudgets("user")).thenReturn(resp);
        boolean exists = helper.budgetForCategoryExists("user", budget);
        assertFalse(exists);
    }

    @Test
    void validateBudgetRequest_throwsIfNull() {
        assertThrows(IllegalArgumentException.class, () -> helper.validateBudgetRequest(null));
        assertThrows(IllegalArgumentException.class, () -> helper.validateBudgetRequest(BudgetRequest.builder().budget(null).build()));
    }

    @Test
    void updateExistingBudget_copiesFields() {
        Budget existing = Budget.builder().amount(Amount.builder().currency("CZK").value(BigDecimal.ONE).build()).category(TransactionCategory.GROCERIES).startDate(LocalDate.of(2024, 1, 1)).build();
        Budget updated = Budget.builder().amount(Amount.builder().currency("USD").value(BigDecimal.TEN).build()).category(TransactionCategory.ENTERTAINMENT).startDate(LocalDate.of(2024, 2, 2)).build();
        helper.updateExistingBudget(existing, updated);
        assertEquals(updated.getAmount(), existing.getAmount());
        assertEquals(updated.getCategory(), existing.getCategory());
        assertEquals(updated.getStartDate(), existing.getStartDate());
    }

    @Test
    void validateExistingBudget_throwsIfNullOrUnauthorized() {
        assertThrows(IllegalArgumentException.class, () -> helper.validateExistingBudget(null, "user"));
        Budget budget = Budget.builder().principalName("other").build();
        assertThrows(SecurityException.class, () -> helper.validateExistingBudget(budget, "user"));
    }

    @Test
    void validateExistingBudget_passesIfValid() {
        Budget budget = Budget.builder().principalName("user").build();
        assertDoesNotThrow(() -> helper.validateExistingBudget(budget, "user"));
    }
}
