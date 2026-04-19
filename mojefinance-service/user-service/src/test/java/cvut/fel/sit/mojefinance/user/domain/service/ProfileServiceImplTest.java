package cvut.fel.sit.mojefinance.user.domain.service;

import cvut.fel.sit.mojefinance.user.data.repository.UserRepository;
import cvut.fel.sit.mojefinance.user.domain.dto.ProfileDomainResponse;
import cvut.fel.sit.mojefinance.user.domain.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProfileServiceImpl profileService;

    private final String MOCK_USERNAME = "testuser";

    @BeforeEach
    void setUpSecurityContext() {
        // 1. Mock the UserDetails principal
        UserDetails userDetails = mock(UserDetails.class);
        lenient().when(userDetails.getUsername()).thenReturn(MOCK_USERNAME);

        // 2. Mock the Authentication object
        Authentication authentication = mock(Authentication.class);
        lenient().when(authentication.getPrincipal()).thenReturn(userDetails);

        // 3. Mock the Security Context
        SecurityContext securityContext = mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);

        // 4. Inject into the holder
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void clearSecurityContext() {
        // Prevent context leakage between tests
        SecurityContextHolder.clearContext();
    }

    @Test
    void getProfile_ShouldReturnProfileResponseWithCurrentUser() {
        // Arrange
        User mockUser = new User();
        mockUser.setUsername(MOCK_USERNAME);
        mockUser.setEmail("testuser@example.com");

        when(userRepository.getUserByUsername(MOCK_USERNAME)).thenReturn(mockUser);

        // Act
        ProfileDomainResponse response = profileService.getProfile();

        // Assert
        assertNotNull(response, "ProfileDomainResponse should not be null");
        assertNotNull(response.getUser(), "User inside response should not be null");
        assertEquals(MOCK_USERNAME, response.getUser().getUsername());
        assertEquals("testuser@example.com", response.getUser().getEmail());

        // Verify the repository was called with the exact username extracted from the security context
        verify(userRepository, times(1)).getUserByUsername(MOCK_USERNAME);
    }

    @Test
    void getProfile_WhenUserNotFound_ShouldReturnProfileWithNullUser() {
        // Arrange
        when(userRepository.getUserByUsername(MOCK_USERNAME)).thenReturn(null);

        // Act
        ProfileDomainResponse response = profileService.getProfile();

        // Assert
        assertNotNull(response, "ProfileDomainResponse should still be created");
        assertNull(response.getUser(), "User should be null if repository returns null");

        verify(userRepository, times(1)).getUserByUsername(MOCK_USERNAME);
    }
}