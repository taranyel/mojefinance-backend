package cvut.fel.sit.mojefinance.user.domain.dto;

import cvut.fel.sit.mojefinance.user.domain.entity.User;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProfileResponse {
    User user;
}
