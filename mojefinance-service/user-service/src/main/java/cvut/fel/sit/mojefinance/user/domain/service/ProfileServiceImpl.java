package cvut.fel.sit.mojefinance.user.domain.service;

import cvut.fel.sit.mojefinance.user.data.repository.UserRepository;
import cvut.fel.sit.mojefinance.user.domain.dto.ProfileDomainResponse;
import cvut.fel.sit.mojefinance.user.domain.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;


@Service
@Slf4j
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {
    private final UserRepository userRepository;

    @Override
    public ProfileDomainResponse getProfile() {
        log.info("Getting current user details.");

        String username = getUsername();
        User user = userRepository.getUserByUsername(username);
        log.info("User found.");
        return ProfileDomainResponse.builder()
                .user(user)
                .build();
    }

    private String getUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userDetails.getUsername();
    }
}
