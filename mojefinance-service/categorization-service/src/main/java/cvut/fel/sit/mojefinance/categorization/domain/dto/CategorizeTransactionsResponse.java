package cvut.fel.sit.mojefinance.categorization.domain.dto;

import cvut.fel.sit.mojefinance.categorization.domain.entity.TransactionCategory;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Builder
@Data
public class CategorizeTransactionsResponse {
    private Map<String, TransactionCategory> categorizedTransactions;
}
