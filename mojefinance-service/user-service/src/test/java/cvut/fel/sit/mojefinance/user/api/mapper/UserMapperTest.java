package cvut.fel.sit.mojefinance.user.api.mapper;

import cvut.fel.sit.mojefinance.openapi.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    private UserMapper mapper;

    @BeforeEach
    void setUp() {
        // Instantiate the MapStruct-generated implementation directly
        mapper = new UserMapperImpl();
    }

    @Test
    void toUserDomainEntity_WhenValidUser_ShouldMapCorrectly() {
        // Arrange: Create the OpenAPI User model
        User apiUser = new User();
        apiUser.setUsername("jane_doe");
        apiUser.setEmail("jane.doe@example.com");
        apiUser.setFirstName("Jane");
        apiUser.setLastName("Doe");

        // Act: Perform the mapping
        cvut.fel.sit.mojefinance.user.domain.entity.User domainUser = mapper.toUserDomainEntity(apiUser);

        // Assert: Verify all fields mapped successfully
        assertNotNull(domainUser);
        assertEquals("jane_doe", domainUser.getUsername());
        assertEquals("jane.doe@example.com", domainUser.getEmail());
        assertEquals("Jane", domainUser.getFirstName());
        assertEquals("Doe", domainUser.getLastName());
    }

    @Test
    void toUserDomainEntity_WhenInputIsNull_ShouldReturnNull() {
        // Act
        cvut.fel.sit.mojefinance.user.domain.entity.User domainUser = mapper.toUserDomainEntity(null);

        // Assert
        assertNull(domainUser, "Mapper should return null when the input is null to prevent NullPointerExceptions");
    }
}