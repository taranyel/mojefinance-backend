package cvut.fel.sit.mojefinance.budget.api.mapper;

import cvut.fel.sit.mojefinance.budget.domain.entity.BudgetStatus;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import cvut.fel.sit.mojefinance.openapi.model.BudgetRequest;
import cvut.fel.sit.mojefinance.openapi.model.BudgetsResponse;
import cvut.fel.sit.mojefinance.openapi.model.Budget;
import cvut.fel.sit.mojefinance.openapi.model.Amount;
import cvut.fel.sit.shared.entity.TransactionCategory;

class BudgetsMapperTest {
    private BudgetsMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(BudgetsMapper.class);
    }

    @Test
    void toBudgetRequest_mapsFieldsCorrectly() {
        Budget openApiBudget = getBudget();
        BudgetRequest openApiBudgetRequest = new BudgetRequest();
        openApiBudgetRequest.setBudget(openApiBudget);

        cvut.fel.sit.mojefinance.budget.domain.dto.BudgetRequest result = mapper.toBudgetRequest(openApiBudgetRequest);
        assertNotNull(result);
        assertNotNull(result.getBudget());
        assertEquals(1L, result.getBudget().getBudgetId());
        assertEquals(TransactionCategory.GROCERIES, result.getBudget().getCategory());
        assertEquals("CZK", result.getBudget().getAmount().getCurrency());
        assertEquals(new BigDecimal("100.0"), result.getBudget().getAmount().getValue());
        assertEquals(LocalDate.of(2024, 4, 19), result.getBudget().getStartDate());
        assertEquals(BudgetStatus.ACTIVE, result.getBudget().getBudgetStatus());
    }

    private static @NonNull Budget getBudget() {
        Amount openApiAmount = new Amount();
        openApiAmount.setCurrency("CZK");
        openApiAmount.setValue(100.00); // openapi Amount uses Double
        Budget openApiBudget = new Budget();
        openApiBudget.setBudgetId(1);
        openApiBudget.setCategory("GROCERIES");
        openApiBudget.setAmount(openApiAmount);
        openApiBudget.setStartDate(LocalDate.of(2024, 4, 19));
        openApiBudget.setSpentAmount(openApiAmount);
        openApiBudget.setBudgetStatus(Budget.BudgetStatusEnum.ACTIVE);
        return openApiBudget;
    }

    @Test
    void toBudgetsResponse_mapsFieldsCorrectly() {
        cvut.fel.sit.mojefinance.product.domain.entity.Amount amount = cvut.fel.sit.mojefinance.product.domain.entity.Amount.builder().currency("CZK").value(new BigDecimal("200.00")).build();
        cvut.fel.sit.mojefinance.budget.domain.entity.Budget budget = cvut.fel.sit.mojefinance.budget.domain.entity.Budget.builder()
                .budgetId(2L)
                .category(TransactionCategory.ENTERTAINMENT)
                .amount(amount)
                .spentAmount(amount)
                .budgetStatus(BudgetStatus.EXCEEDED)
                .startDate(LocalDate.of(2024, 1, 1))
                .build();
        cvut.fel.sit.mojefinance.budget.domain.dto.BudgetsResponse budgetsResponse = cvut.fel.sit.mojefinance.budget.domain.dto.BudgetsResponse.builder().budgets(List.of(budget)).build();
        BudgetsResponse result = mapper.toBudgetsResponse(budgetsResponse);
        assertNotNull(result);
        assertNotNull(result.getBudgets());
        assertEquals(1, result.getBudgets().size());
        Budget mappedBudget = result.getBudgets().get(0);
        assertEquals(2, mappedBudget.getBudgetId());
        assertEquals("ENTERTAINMENT", mappedBudget.getCategory());
        assertEquals("CZK", mappedBudget.getAmount().getCurrency());
        assertEquals(new BigDecimal("200.0"), BigDecimal.valueOf(mappedBudget.getAmount().getValue()));
        assertEquals(LocalDate.of(2024, 1, 1), mappedBudget.getStartDate());
        assertEquals(Budget.BudgetStatusEnum.EXCEEDED, mappedBudget.getBudgetStatus());
    }
}
