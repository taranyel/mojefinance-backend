package cvut.fel.sit.mojefinance.user.data.repository;

import cvut.fel.sit.mojefinance.user.data.entity.UserEntity;
import cvut.fel.sit.mojefinance.user.data.mapper.UserDataMapper;
import cvut.fel.sit.mojefinance.user.domain.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRepositoryImplTest {

    @Mock
    private UserJpaRepository userJpaRepository;

    @Mock
    private UserDataMapper mapper;

    @InjectMocks
    private UserRepositoryImpl userRepository;

    @Test
    void createUser_ShouldSaveUserEntity() {
        // Arrange
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername("testuser");
        userEntity.setEmail("test@example.com");

        // Act
        userRepository.createUser(userEntity);

        // Assert
        // Verify that the JPA repository's save method was called exactly once with our entity
        verify(userJpaRepository, times(1)).save(userEntity);
    }

    @Test
    void getUserByUsername_WhenUserExists_ShouldReturnMappedUser() {
        // Arrange
        String username = "johndoe";

        UserEntity mockEntity = new UserEntity();
        mockEntity.setUsername(username);

        User mockDomainUser = new User();
        mockDomainUser.setUsername(username);

        // Define mock behavior
        when(userJpaRepository.findByUsername(username)).thenReturn(mockEntity);
        when(mapper.toUser(mockEntity)).thenReturn(mockDomainUser);

        // Act
        User result = userRepository.getUserByUsername(username);

        // Assert
        assertNotNull(result);
        assertEquals(username, result.getUsername());

        // Verify the interactions
        verify(userJpaRepository, times(1)).findByUsername(username);
        verify(mapper, times(1)).toUser(mockEntity);
    }

    @Test
    void getUserByUsername_WhenUserDoesNotExist_ShouldReturnNull() {
        // Arrange
        String username = "unknown_user";

        // Define mock behavior for a missing user
        when(userJpaRepository.findByUsername(username)).thenReturn(null);
        when(mapper.toUser(null)).thenReturn(null);

        // Act
        User result = userRepository.getUserByUsername(username);

        // Assert
        assertNull(result, "Repository should return null when the user is not found");

        // Verify the interactions
        verify(userJpaRepository, times(1)).findByUsername(username);
        verify(mapper, times(1)).toUser(null);
    }
}