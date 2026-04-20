package cvut.fel.sit.mojefinance.budget.data.mapper;

import cvut.fel.sit.mojefinance.budget.data.entity.BudgetEntity;
import cvut.fel.sit.mojefinance.budget.domain.entity.Budget;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class BudgetDataMapperTest {
    private BudgetDataMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(BudgetDataMapper.class);
    }

    @Test
    void toBudget_mapsFieldsCorrectly() {
        BudgetEntity entity = new BudgetEntity();
        entity.setBudgetId(42L);
        entity.setPrincipalName("John Doe");
        entity.setCategory("GROCERIES");
        entity.setAmount(new BigDecimal("123.45"));
        entity.setCurrency("CZK");
        entity.setStartDate(LocalDate.of(2024, 4, 19));

        Budget result = mapper.toBudget(entity);
        assertNotNull(result);
        assertEquals(42L, result.getBudgetId());
        assertEquals("John Doe", result.getPrincipalName());
        assertEquals("GROCERIES", result.getCategory().name()); // assuming TransactionCategory.valueOf
        assertEquals("CZK", result.getAmount().getCurrency());
        assertEquals(new BigDecimal("123.45"), result.getAmount().getValue());
        assertEquals(LocalDate.of(2024, 4, 19), result.getStartDate());
    }
}
