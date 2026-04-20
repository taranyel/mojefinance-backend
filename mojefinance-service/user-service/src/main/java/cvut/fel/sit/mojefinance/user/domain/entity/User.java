package cvut.fel.sit.mojefinance.user.domain.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class User {
    private String username;
    private String email;
    private String firstName;
    private String lastName;
}
