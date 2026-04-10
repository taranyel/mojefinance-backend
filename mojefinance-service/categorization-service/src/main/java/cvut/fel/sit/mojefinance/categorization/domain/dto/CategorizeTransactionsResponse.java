package cvut.fel.sit.mojefinance.categorization.domain.dto;

import cvut.fel.sit.shared.util.entity.TransactionCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategorizeTransactionsResponse {
    private Map<String, TransactionCategory> categorizedTransactions;
}
