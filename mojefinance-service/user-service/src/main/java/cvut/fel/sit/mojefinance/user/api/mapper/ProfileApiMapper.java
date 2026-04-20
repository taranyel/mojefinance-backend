package cvut.fel.sit.mojefinance.user.api.mapper;

import cvut.fel.sit.mojefinance.user.domain.dto.ProfileResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProfileApiMapper {
    cvut.fel.sit.mojefinance.openapi.model.ProfileResponse toProfileResponse(ProfileResponse profileResponse);
}
