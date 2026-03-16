package cvut.fel.sit.mojefinance.product.messaging.dto;

import cvut.fel.sit.mojefinance.product.domain.entity.Transaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionsMessagingResponse {
    private List<Transaction> transactions;
}
