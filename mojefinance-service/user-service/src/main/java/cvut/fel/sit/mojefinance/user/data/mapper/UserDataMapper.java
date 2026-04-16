package cvut.fel.sit.mojefinance.user.data.mapper;

import cvut.fel.sit.mojefinance.user.data.entity.UserEntity;
import cvut.fel.sit.mojefinance.user.domain.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserDataMapper {
    User toUser(UserEntity userEntity);
}
