package cvut.fel.sit.mojefinance.user.api.mapper;

import cvut.fel.sit.mojefinance.openapi.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    cvut.fel.sit.mojefinance.user.domain.entity.User toUserDomainEntity(User user);
}
