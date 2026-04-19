package cvut.fel.sit.mojefinance.user.api.mapper;

import cvut.fel.sit.mojefinance.openapi.model.ProfileResponse;
import cvut.fel.sit.mojefinance.openapi.model.User;
import cvut.fel.sit.mojefinance.user.domain.dto.ProfileDomainResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProfileApiMapperTest {

    private ProfileApiMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ProfileApiMapperImpl();
    }

    @Test
    void toProfileResponse_WhenGivenValidDomainResponse_ShouldMapCorrectly() {
        // Arrange: Create the inner domain User
        cvut.fel.sit.mojefinance.user.domain.entity.User domainUser = new cvut.fel.sit.mojefinance.user.domain.entity.User();
        domainUser.setFirstName("John");
        domainUser.setLastName("Doe");
        domainUser.setUsername("johndoe");
        domainUser.setEmail("john.doe@example.com");

        // Arrange: Create the wrapping Domain Response
        ProfileDomainResponse domainResponse = ProfileDomainResponse.builder().build();
        domainResponse.setUser(domainUser);

        // Act
        ProfileResponse apiResponse = mapper.toProfileResponse(domainResponse);

        // Assert
        assertNotNull(apiResponse);
        assertNotNull(apiResponse.getUser());

        User apiUser = apiResponse.getUser();
        assertEquals("John", apiUser.getFirstName());
        assertEquals("Doe", apiUser.getLastName());
        assertEquals("johndoe", apiUser.getUsername());
        assertEquals("john.doe@example.com", apiUser.getEmail());
    }

    @Test
    void toProfileResponse_WhenInputIsNull_ShouldReturnNull() {
        // Act
        ProfileResponse apiResponse = mapper.toProfileResponse(null);

        // Assert
        assertNull(apiResponse, "Mapper should safely return null when input is null");
    }

    @Test
    void toProfileResponse_WhenInnerUserIsNull_ShouldReturnResponseWithNullUser() {
        // Arrange
        ProfileDomainResponse domainResponse = ProfileDomainResponse.builder().build();
        domainResponse.setUser(null); // Explicitly null inner user

        // Act
        ProfileResponse apiResponse = mapper.toProfileResponse(domainResponse);

        // Assert
        assertNotNull(apiResponse, "Response wrapper should not be null");
        assertNull(apiResponse.getUser(), "Inner user should be mapped safely as null");
    }
}