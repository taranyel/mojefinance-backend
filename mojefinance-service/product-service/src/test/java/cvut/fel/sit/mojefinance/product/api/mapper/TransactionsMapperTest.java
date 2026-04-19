package cvut.fel.sit.mojefinance.product.api.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.Collections;
import java.math.BigDecimal;
import cvut.fel.sit.mojefinance.openapi.model.TransactionsResponse;
import cvut.fel.sit.mojefinance.openapi.model.GroupedTransactions;

class TransactionsMapperTest {
    private TransactionsMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(TransactionsMapper.class);
    }

    @Test
    void toTransactionsResponse_mapsFieldsCorrectly() {
        // Domain model setup
        cvut.fel.sit.mojefinance.product.domain.entity.Amount income = cvut.fel.sit.mojefinance.product.domain.entity.Amount.builder().currency("CZK").value(new BigDecimal("1000.00")).build();
        cvut.fel.sit.mojefinance.product.domain.entity.Amount expense = cvut.fel.sit.mojefinance.product.domain.entity.Amount.builder().currency("CZK").value(new BigDecimal("500.00")).build();
        cvut.fel.sit.mojefinance.product.domain.entity.Transaction transaction = cvut.fel.sit.mojefinance.product.domain.entity.Transaction.builder()
                .counterpartyName("Alice")
                .build();
        cvut.fel.sit.mojefinance.product.domain.entity.GroupedTransactions grouped = cvut.fel.sit.mojefinance.product.domain.entity.GroupedTransactions.builder()
                .groupName("Group1")
                .totalIncome(income)
                .totalExpense(expense)
                .transactions(List.of(transaction))
                .groupedTransactions(Collections.emptyList())
                .build();
        cvut.fel.sit.mojefinance.product.domain.dto.TransactionsDomainResponse domainResponse = cvut.fel.sit.mojefinance.product.domain.dto.TransactionsDomainResponse.builder()
                .groupedTransactions(List.of(grouped)).build();

        // Mapstruct mapping
        TransactionsResponse result = mapper.toTransactionsResponse(domainResponse);
        assertNotNull(result);
        assertNotNull(result.getGroupedTransactions());
        assertEquals(1, result.getGroupedTransactions().size());
        GroupedTransactions mappedGroup = result.getGroupedTransactions().get(0);
        assertEquals("Group1", mappedGroup.getGroupName());
        assertEquals("CZK", mappedGroup.getTotalIncome().getCurrency());
        assertEquals(1000.00, mappedGroup.getTotalIncome().getValue());
        assertEquals("CZK", mappedGroup.getTotalExpense().getCurrency());
        assertEquals(500.00, mappedGroup.getTotalExpense().getValue());
        assertEquals(1, mappedGroup.getTransactions().size());
        assertEquals("Alice", mappedGroup.getTransactions().get(0).getCounterpartyName());
        assertTrue(mappedGroup.getGroupedTransactions().isEmpty());
    }
}
