package cvut.fel.sit.mojefinance.user.domain.service;

import cvut.fel.sit.mojefinance.user.data.entity.UserEntity;
import cvut.fel.sit.mojefinance.user.data.repository.UserRepository;
import cvut.fel.sit.mojefinance.user.domain.entity.User;
import cvut.fel.sit.mojefinance.user.domain.mapper.UserDomainMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {
    private final UserRepository userRepository;
    private final UserDomainMapper userDomainMapper;

    @Override
    public void registerUser(User user) {
        log.info("Registering new user.");
        UserEntity newUserEntity = userDomainMapper.toUserEntity(user);
        userRepository.createUser(newUserEntity);
        log.info("User registered successfully");
    }
}
