package cvut.fel.sit.mojefinance.categorization.domain.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class CategorizeTransactionsRequest {
    private Set<String> transactionsNames;
}
