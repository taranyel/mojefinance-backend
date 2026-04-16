package cvut.fel.sit.mojefinance.product.domain.dto;

import cvut.fel.sit.mojefinance.product.domain.entity.Budget;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetRequest {
    private Budget budget;
}
