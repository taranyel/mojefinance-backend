package cvut.fel.sit.mojefinance.product.domain.dto;

import cvut.fel.sit.mojefinance.product.domain.entity.Budget;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetsResponse {
    private List<Budget> budgets;
}
