package cvut.fel.sit.mojefinance.user.data.repository;

import cvut.fel.sit.mojefinance.user.data.entity.UserEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserRepositoryImpl implements UserRepository {
    private final UserJpaRepository userJpaRepository;

    @Override
    public void createUser(UserEntity user) {
        userJpaRepository.save(user);
    }

    @Override
    public UserEntity getUserByUsername(String username) {
        return userJpaRepository.findByUsername(username);
    }
}
