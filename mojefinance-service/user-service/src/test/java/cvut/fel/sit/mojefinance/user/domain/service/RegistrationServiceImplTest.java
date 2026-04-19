package cvut.fel.sit.mojefinance.user.domain.service;

import cvut.fel.sit.mojefinance.user.data.entity.UserEntity;
import cvut.fel.sit.mojefinance.user.data.repository.UserRepository;
import cvut.fel.sit.mojefinance.user.domain.entity.User;
import cvut.fel.sit.mojefinance.user.domain.mapper.UserDomainMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserDomainMapper userDomainMapper;
    @InjectMocks
    private RegistrationServiceImpl registrationService;

    @Test
    void registerUser_ShouldMapAndSaveUser() {
        User domainUser = new User();
        domainUser.setUsername("testUser");
        UserEntity mappedEntity = new UserEntity();
        mappedEntity.setUsername("testUser");
        when(userDomainMapper.toUserEntity(domainUser)).thenReturn(mappedEntity);
        registrationService.registerUser(domainUser);
        verify(userDomainMapper, times(1)).toUserEntity(domainUser);
        verify(userRepository, times(1)).createUser(mappedEntity);
    }

    @Test
    void registerUser_WhenUserIsNull_ShouldThrowException() {
        when(userDomainMapper.toUserEntity(null)).thenReturn(null);
        registrationService.registerUser(null);
        verify(userRepository, times(1)).createUser(null);
    }
}