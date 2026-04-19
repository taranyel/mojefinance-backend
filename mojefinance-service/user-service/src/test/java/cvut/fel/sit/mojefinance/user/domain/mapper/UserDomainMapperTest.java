package cvut.fel.sit.mojefinance.user.domain.mapper;

import cvut.fel.sit.mojefinance.user.data.entity.UserEntity;
import cvut.fel.sit.mojefinance.user.domain.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserDomainMapperTest {

    private UserDomainMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new UserDomainMapperImpl();
    }

    @Test
    void toUserEntity_WhenValidUser_ShouldMapCorrectly() {
        // Arrange
        User domainUser = new User();
        domainUser.setUsername("alice123");
        domainUser.setFirstName("Alice");
        domainUser.setLastName("Wonderland");
        domainUser.setEmail("alice@example.com");

        // Act
        UserEntity entity = mapper.toUserEntity(domainUser);

        // Assert
        assertNotNull(entity);
        assertEquals("alice123", entity.getUsername());
        assertEquals("Alice", entity.getFirstName());
        assertEquals("Wonderland", entity.getLastName());
        assertEquals("alice@example.com", entity.getEmail());
    }

    @Test
    void toUserEntity_WhenInputIsNull_ShouldReturnNull() {
        // Act
        UserEntity entity = mapper.toUserEntity(null);

        // Assert
        assertNull(entity, "Mapper should safely return null to prevent NullPointerExceptions");
    }

    @Test
    void toUserEntity_WhenPartialData_ShouldMapSafely() {
        // Arrange
        User domainUser = new User();
        domainUser.setUsername("bob456");
        // Leave other fields null

        // Act
        UserEntity entity = mapper.toUserEntity(domainUser);

        // Assert
        assertNotNull(entity);
        assertEquals("bob456", entity.getUsername());
        assertNull(entity.getFirstName());
        assertNull(entity.getLastName());
        assertNull(entity.getEmail());
    }
}