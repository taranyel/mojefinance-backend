package cvut.fel.sit.mojefinance.user.api.controller;

import cvut.fel.sit.mojefinance.openapi.api.ProfileApi;
import cvut.fel.sit.mojefinance.user.api.mapper.ProfileApiMapper;
import cvut.fel.sit.mojefinance.user.domain.dto.ProfileResponse;
import cvut.fel.sit.mojefinance.user.domain.service.ProfileService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class ProfileController implements ProfileApi {
    private final ProfileService profileService;
    private final ProfileApiMapper mapper;

    @Override
    public ResponseEntity<cvut.fel.sit.mojefinance.openapi.model.ProfileResponse> getCurrentUser(String authorization) {
        ProfileResponse profileDomainResponse = profileService.getProfile();
        cvut.fel.sit.mojefinance.openapi.model.ProfileResponse profileResponse = mapper.toProfileResponse(profileDomainResponse);
        return new ResponseEntity<>(profileResponse, HttpStatus.OK);
    }
}
