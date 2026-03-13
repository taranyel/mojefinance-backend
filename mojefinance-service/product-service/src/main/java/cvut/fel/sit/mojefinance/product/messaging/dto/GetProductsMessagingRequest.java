package cvut.fel.sit.mojefinance.product.messaging.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetProductsMessagingRequest {
    private String authorization;
    private String clientRegistrationId;
}
