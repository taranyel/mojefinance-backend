package cvut.fel.sit.mojefinance.budget.domain.entity;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import cvut.fel.sit.mojefinance.product.domain.entity.Amount;
import cvut.fel.sit.shared.entity.TransactionCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Budget {
    private Long budgetId;
    private String principalName;
    private TransactionCategory category;
    private Amount amount;
    private Amount spentAmount;
    private BudgetStatus budgetStatus;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate startDate;
}
