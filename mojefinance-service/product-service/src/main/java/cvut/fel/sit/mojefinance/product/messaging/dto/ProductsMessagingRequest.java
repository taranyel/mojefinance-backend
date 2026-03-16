package cvut.fel.sit.mojefinance.product.messaging.dto;

import cvut.fel.sit.mojefinance.product.domain.entity.BankDetails;import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductsMessagingRequest {
    private String authorization;
    private BankDetails bankDetails;
    private String principalName;
}
