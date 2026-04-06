package cvut.fel.sit.mojefinance.user.api.mapper;

import cvut.fel.sit.mojefinance.openapi.model.ProfileResponse;
import cvut.fel.sit.mojefinance.user.domain.dto.ProfileDomainResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProfileApiMapper {
    ProfileResponse toProfileResponse(ProfileDomainResponse profileDomainResponse);
}
