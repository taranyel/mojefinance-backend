package cvut.fel.sit.mojefinance.authorization.data.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthorizedClientServiceRequest {
    private String principalName;
    private String clientRegistrationId;
}
