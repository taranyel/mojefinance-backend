package cvut.fel.sit.mojefinance.user.domain.service;

import cvut.fel.sit.mojefinance.user.domain.dto.ProfileResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileServiceImplTest {

    @InjectMocks
    private ProfileServiceImpl profileService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private BearerTokenAuthentication jwtAuthenticationToken;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getProfile_WithValidJwtToken_ShouldReturnProfileResponse() {
        Map<String, Object> mockClaims = Map.of(
                "preferred_username", "johndoe",
                "email", "john.doe@example.com",
                "given_name", "John",
                "family_name", "Doe"
        );

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getTokenAttributes()).thenReturn(mockClaims);

        ProfileResponse response = profileService.getProfile();

        assertThat(response).isNotNull();
        assertThat(response.getUser()).isNotNull();
        assertThat(response.getUser().getUsername()).isEqualTo("johndoe");
        assertThat(response.getUser().getEmail()).isEqualTo("john.doe@example.com");
        assertThat(response.getUser().getFirstName()).isEqualTo("John");
        assertThat(response.getUser().getLastName()).isEqualTo("Doe");
    }

    @Test
    void getProfile_WithNonJwtAuthentication_ShouldThrowIllegalStateException() {
        UsernamePasswordAuthenticationToken wrongAuthType =
                new UsernamePasswordAuthenticationToken("user", "password");

        when(securityContext.getAuthentication()).thenReturn(wrongAuthType);

        assertThatThrownBy(() -> profileService.getProfile())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("User is not authenticated with a JWT");
    }

    @Test
    void getProfile_WithNullAuthentication_ShouldThrowIllegalStateException() {
        when(securityContext.getAuthentication()).thenReturn(null);
        assertThatThrownBy(() -> profileService.getProfile())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("User is not authenticated with a JWT");
    }
}