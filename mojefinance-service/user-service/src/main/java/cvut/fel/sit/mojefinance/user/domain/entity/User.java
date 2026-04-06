package cvut.fel.sit.mojefinance.user.domain.entity;

import lombok.Data;

@Data
public class User {
    private String username;
    private String email;
    private String firstName;
    private String lastName;
}
