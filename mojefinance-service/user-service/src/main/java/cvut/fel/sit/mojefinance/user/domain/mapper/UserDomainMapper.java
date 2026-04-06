package cvut.fel.sit.mojefinance.user.domain.mapper;

import cvut.fel.sit.mojefinance.user.data.entity.UserEntity;
import cvut.fel.sit.mojefinance.user.domain.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserDomainMapper {
    User toUser(UserEntity userEntity);

    UserEntity toUserEntity(User user);
}
