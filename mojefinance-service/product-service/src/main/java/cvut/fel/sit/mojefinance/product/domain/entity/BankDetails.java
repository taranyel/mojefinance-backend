package cvut.fel.sit.mojefinance.product.domain.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BankDetails {
    private String clientRegistrationId;
    private String bankName;
}
