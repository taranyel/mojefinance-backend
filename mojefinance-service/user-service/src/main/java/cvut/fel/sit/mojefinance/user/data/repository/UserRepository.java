package cvut.fel.sit.mojefinance.user.data.repository;

import cvut.fel.sit.mojefinance.user.data.entity.UserEntity;
import cvut.fel.sit.mojefinance.user.domain.entity.User;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository {
    void createUser(UserEntity user);

    User getUserByUsername(String username);
}
