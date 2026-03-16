package cvut.fel.sit.mojefinance.product.domain.dto;

import cvut.fel.sit.mojefinance.product.domain.entity.BankDetails;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccountInfoRequest {
    private String authorization;
    private BankDetails bankDetails;
    private String accountId;
    private String principalName;
}
