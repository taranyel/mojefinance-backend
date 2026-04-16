package cvut.fel.sit.mojefinance.user.data.repository;

import cvut.fel.sit.mojefinance.user.data.entity.UserEntity;
import cvut.fel.sit.mojefinance.user.data.mapper.UserDataMapper;
import cvut.fel.sit.mojefinance.user.domain.entity.User;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserRepositoryImpl implements UserRepository {
    private final UserJpaRepository userJpaRepository;
    private final UserDataMapper mapper;

    @Override
    public void createUser(UserEntity user) {
        userJpaRepository.save(user);
    }

    @Override
    public User getUserByUsername(String username) {
        UserEntity userEntity = userJpaRepository.findByUsername(username);
        return mapper.toUser(userEntity);
    }
}
