package cvut.fel.sit.mojefinance.budget.domain.mapper;

import cvut.fel.sit.mojefinance.budget.data.entity.BudgetEntity;
import cvut.fel.sit.mojefinance.budget.domain.entity.Budget;
import cvut.fel.sit.mojefinance.product.domain.entity.Amount;
import cvut.fel.sit.shared.entity.TransactionCategory; // Assuming this based on previous context
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class BudgetDomainMapperTest {

    private BudgetDomainMapper mapper;

    @BeforeEach
    void setUp() {
        // Instantiate the generated Impl directly for fast, pure unit testing
        // without needing to spin up the heavy Spring Context.
        mapper = new BudgetDomainMapperImpl();
    }

    @Test
    void toBudgetEntity_WhenValidBudget_ShouldMapCorrectly() {
        // 1. Arrange: Create the nested Amount object
        Amount amount = new Amount();
        amount.setValue(new BigDecimal("1500.50"));
        amount.setCurrency("CZK");

        // 2. Arrange: Create the main Budget object
        Budget budget = new Budget();
        budget.setBudgetId(100L);
        budget.setAmount(amount);
        budget.setCategory(TransactionCategory.GROCERIES);
        budget.setStartDate(LocalDate.of(2026, 4, 1));
        budget.setPrincipalName("user123");

        // 3. Act: Perform the mapping
        BudgetEntity entity = mapper.toBudgetEntity(budget);

        // 4. Assert: Verify all fields, especially the nested amount/currency mappings
        assertNotNull(entity);
        assertEquals(100L, entity.getBudgetId());
        assertEquals(new BigDecimal("1500.50"), entity.getAmount());
        assertEquals("CZK", entity.getCurrency());
        assertEquals("GROCERIES", entity.getCategory());
        assertEquals(LocalDate.of(2026, 4, 1), entity.getStartDate());
        assertEquals("user123", entity.getPrincipalName());
    }

    @Test
    void toBudgetEntity_WhenBudgetIsNull_ShouldReturnNull() {
        // Act
        BudgetEntity entity = mapper.toBudgetEntity(null);

        // Assert
        assertNull(entity);
    }

    @Test
    void toBudgetEntity_WhenAmountIsNull_ShouldMapSafelyAndLeaveAmountNull() {
        // Arrange
        Budget budget = new Budget();
        budget.setBudgetId(200L);
        budget.setPrincipalName("user456");
        budget.setAmount(null); // Explicitly null

        // Act
        BudgetEntity entity = mapper.toBudgetEntity(budget);

        // Assert
        assertNotNull(entity);
        assertEquals(200L, entity.getBudgetId());
        assertEquals("user456", entity.getPrincipalName());
        assertNull(entity.getAmount()); // Verify safe null handling
        assertNull(entity.getCurrency()); // Verify safe null handling
    }

    @Test
    void toBudgetEntity_WhenCategoryIsNull_ShouldMapSafely() {
        // Arrange
        Budget budget = new Budget();
        budget.setBudgetId(300L);
        budget.setCategory(null); // Explicitly null

        // Act
        BudgetEntity entity = mapper.toBudgetEntity(budget);

        // Assert
        assertNotNull(entity);
        assertEquals(300L, entity.getBudgetId());
        assertNull(entity.getCategory()); // Verify enum safe null handling
    }
}