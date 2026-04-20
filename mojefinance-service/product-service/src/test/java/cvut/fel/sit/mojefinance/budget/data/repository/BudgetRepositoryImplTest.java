package cvut.fel.sit.mojefinance.budget.data.repository;

import cvut.fel.sit.mojefinance.budget.data.entity.BudgetEntity;
import cvut.fel.sit.mojefinance.budget.data.mapper.BudgetDataMapper;
import cvut.fel.sit.mojefinance.budget.domain.dto.BudgetsResponse;
import cvut.fel.sit.mojefinance.budget.domain.entity.Budget;
import cvut.fel.sit.mojefinance.budget.domain.entity.BudgetStatus;
import cvut.fel.sit.mojefinance.product.domain.entity.Amount;
import cvut.fel.sit.shared.entity.TransactionCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BudgetRepositoryImplTest {
    @Mock
    private BudgetJpaRepository budgetJpaRepository;
    @Mock
    private BudgetDataMapper budgetDataMapper;
    @InjectMocks
    private BudgetRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        repository = new BudgetRepositoryImpl(budgetJpaRepository, budgetDataMapper);
    }

    @Test
    void saveBudget_delegatesToJpaRepository() {
        BudgetEntity entity = new BudgetEntity();
        repository.saveBudget(entity, "user");
        verify(budgetJpaRepository).save(entity);
    }

    @Test
    void getBudgets_returnsMappedBudgets() {
        BudgetEntity entity = new BudgetEntity();
        entity.setBudgetId(1L);
        entity.setPrincipalName("user");
        entity.setCategory("GROCERIES");
        entity.setAmount(new BigDecimal("100.00"));
        entity.setCurrency("CZK");
        entity.setStartDate(LocalDate.of(2024, 4, 19));
        when(budgetJpaRepository.findAllByPrincipalName("user")).thenReturn(List.of(entity));
        Budget budget = Budget.builder()
                .budgetId(1L)
                .principalName("user")
                .category(TransactionCategory.GROCERIES)
                .amount(Amount.builder().currency("CZK").value(new BigDecimal("100.00")).build())
                .budgetStatus(BudgetStatus.ACTIVE)
                .startDate(LocalDate.of(2024, 4, 19))
                .build();
        when(budgetDataMapper.toBudget(entity)).thenReturn(budget);
        BudgetsResponse response = repository.getBudgets("user");
        assertNotNull(response);
        assertEquals(1, response.getBudgets().size());
        assertEquals("user", response.getBudgets().get(0).getPrincipalName());
        assertEquals(TransactionCategory.GROCERIES, response.getBudgets().get(0).getCategory());
    }

    @Test
    void deleteBudget_delegatesToJpaRepository() {
        repository.deleteBudget(5L, "user");
        verify(budgetJpaRepository).deleteById(5L);
    }

    @Test
    void getBudgetById_returnsMappedBudget() {
        BudgetEntity entity = new BudgetEntity();
        entity.setBudgetId(2L);
        entity.setPrincipalName("user2");
        entity.setCategory("GROCERIES");
        entity.setAmount(new BigDecimal("200.00"));
        entity.setCurrency("USD");
        entity.setStartDate(LocalDate.of(2024, 1, 1));
        when(budgetJpaRepository.findById(2L)).thenReturn(Optional.of(entity));
        Budget budget = Budget.builder()
                .budgetId(2L)
                .principalName("user2")
                .category(TransactionCategory.GROCERIES)
                .amount(Amount.builder().currency("USD").value(new BigDecimal("200.00")).build())
                .budgetStatus(BudgetStatus.EXCEEDED)
                .startDate(LocalDate.of(2024, 1, 1))
                .build();
        when(budgetDataMapper.toBudget(entity)).thenReturn(budget);
        Budget result = repository.getBudgetById(2L);
        assertNotNull(result);
        assertEquals("user2", result.getPrincipalName());
        assertEquals(TransactionCategory.GROCERIES, result.getCategory());
        assertEquals(new BigDecimal("200.00"), result.getAmount().getValue());
    }

    @Test
    void getBudgetById_returnsNullIfNotFound() {
        when(budgetJpaRepository.findById(99L)).thenReturn(Optional.empty());
        Budget result = repository.getBudgetById(99L);
        assertNull(result);
    }
}
