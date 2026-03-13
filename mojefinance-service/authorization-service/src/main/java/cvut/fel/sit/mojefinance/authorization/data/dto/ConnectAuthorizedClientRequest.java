package cvut.fel.sit.mojefinance.authorization.data.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConnectAuthorizedClientRequest {
    private String code;
    private String clientRegistrationId;
}
