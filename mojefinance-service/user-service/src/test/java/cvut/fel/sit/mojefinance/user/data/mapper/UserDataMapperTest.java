package cvut.fel.sit.mojefinance.user.data.mapper;

import cvut.fel.sit.mojefinance.user.data.entity.UserEntity;
import cvut.fel.sit.mojefinance.user.domain.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserDataMapperTest {

    private UserDataMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new UserDataMapperImpl();
    }

    @Test
    void toUser_WhenValidEntity_ShouldMapCorrectly() {
        // Arrange
        UserEntity entity = new UserEntity();
        entity.setUsername("johndoe");
        entity.setEmail("john.doe@example.com");
        entity.setFirstName("John");
        entity.setLastName("Doe");

        // Act
        User domainUser = mapper.toUser(entity);

        // Assert
        assertNotNull(domainUser);
        assertEquals("johndoe", domainUser.getUsername());
        assertEquals("john.doe@example.com", domainUser.getEmail());
        assertEquals("John", domainUser.getFirstName());
        assertEquals("Doe", domainUser.getLastName());
    }

    @Test
    void toUser_WhenInputIsNull_ShouldReturnNull() {
        // Act
        User domainUser = mapper.toUser(null);

        // Assert
        assertNull(domainUser, "Mapper should return null to avoid NullPointerExceptions");
    }

    @Test
    void toUser_WhenPartialData_ShouldMapSafely() {
        // Arrange
        UserEntity entity = new UserEntity();
        entity.setUsername("only_username");
        // Leave other fields null

        // Act
        User domainUser = mapper.toUser(entity);

        // Assert
        assertNotNull(domainUser);
        assertEquals("only_username", domainUser.getUsername());
        assertNull(domainUser.getEmail());
        assertNull(domainUser.getFirstName());
        assertNull(domainUser.getLastName());
    }
}