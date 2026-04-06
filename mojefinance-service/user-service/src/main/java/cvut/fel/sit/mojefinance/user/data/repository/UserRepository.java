package cvut.fel.sit.mojefinance.user.data.repository;

import cvut.fel.sit.mojefinance.user.data.entity.UserEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository {
    void createUser(UserEntity user);

    UserEntity getUserByUsername(String username);
}
