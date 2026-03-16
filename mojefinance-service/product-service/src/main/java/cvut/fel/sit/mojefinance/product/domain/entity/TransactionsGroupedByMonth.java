package cvut.fel.sit.mojefinance.product.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionsGroupedByMonth {
    private String groupName;
    private List<TransactionsGroupedByCategory> groupedByCategory;
}
