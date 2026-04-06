package cvut.fel.sit.mojefinance.user.api.controller;

import cvut.fel.sit.mojefinance.openapi.api.RegistrationApi;
import cvut.fel.sit.mojefinance.openapi.model.User;
import cvut.fel.sit.mojefinance.user.api.mapper.UserMapper;
import cvut.fel.sit.mojefinance.user.domain.service.RegistrationService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class RegistrationController implements RegistrationApi {
    private final RegistrationService registrationService;
    private final UserMapper userMapper;

    @Override
    public ResponseEntity<Void> registerUser(User user) {
        cvut.fel.sit.mojefinance.user.domain.entity.User userDomainEntity = userMapper.toUserDomainEntity(user);
        registrationService.registerUser(userDomainEntity);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
