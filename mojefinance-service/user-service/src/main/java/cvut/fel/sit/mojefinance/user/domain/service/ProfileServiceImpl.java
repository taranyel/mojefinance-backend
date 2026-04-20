package cvut.fel.sit.mojefinance.user.domain.service;

import cvut.fel.sit.mojefinance.user.domain.dto.ProfileResponse;
import cvut.fel.sit.mojefinance.user.domain.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service
@Slf4j
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    @Override
    public ProfileResponse getProfile() {
        log.info("Getting current user details.");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtToken) {
            Map<String, Object> claims = jwtToken.getTokenAttributes();

            String username = (String) claims.get("preferred_username");
            String email = (String) claims.get("email");
            String firstName = (String) claims.get("given_name");
            String lastName = (String) claims.get("family_name");

            return ProfileResponse.builder()
                    .user(User.builder()
                            .email(email)
                            .lastName(lastName)
                            .firstName(firstName)
                            .username(username)
                            .build())
                    .build();
        }
        throw new IllegalStateException("User is not authenticated with a JWT");
    }
}
