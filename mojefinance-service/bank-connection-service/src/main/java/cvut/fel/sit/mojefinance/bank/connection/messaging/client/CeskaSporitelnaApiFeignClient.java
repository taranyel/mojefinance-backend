package cvut.fel.sit.mojefinance.bank.connection.messaging.client;

import cvut.fel.sit.cs.openapi.model.AuthCodeResponse;
import cvut.fel.sit.mojefinance.bank.connection.messaging.config.BankConnectionFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "CeskaSporitelnaApiFeignClient",
        url = "${external.api.ceska-sporitelna.base-url}",
        configuration = BankConnectionFeignConfig.class
)
public interface CeskaSporitelnaApiFeignClient {

    @PostMapping(value = "${external.api.ceska-sporitelna.token-path}",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    ResponseEntity<AuthCodeResponse> getToken(
            @RequestBody MultiValueMap<String, String> formData
    );
}
