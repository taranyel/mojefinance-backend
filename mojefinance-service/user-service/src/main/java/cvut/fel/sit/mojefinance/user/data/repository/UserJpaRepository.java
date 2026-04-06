package cvut.fel.sit.mojefinance.user.data.repository;

import cvut.fel.sit.mojefinance.user.data.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {
    UserEntity findByUsername(String username);
}
