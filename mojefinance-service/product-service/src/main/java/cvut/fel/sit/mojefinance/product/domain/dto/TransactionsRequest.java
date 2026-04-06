package cvut.fel.sit.mojefinance.product.domain.dto;

import cvut.fel.sit.mojefinance.product.domain.entity.BankDetails;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class TransactionsRequest {
    private String authorization;
    private BankDetails bankDetails;
    private String accountId;
    private String principalName;
    private LocalDate fromDate;
    private LocalDate toDate;
}
