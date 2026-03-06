package cvut.fel.sit.mojefinance.external.api.gateway.messaging.kb.client;

import cvut.fel.sit.cs.openapi.model.AuthCodeResponse;
import cvut.fel.sit.mojefinance.external.api.gateway.messaging.kb.config.KBFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "KBApiFeignClient",
        url = "${external.api.kb.base-url}",
        configuration = KBFeignConfig.class
)
public interface KBApiFeignClient {

    @PostMapping(value = "${external.api.kb.token-path}",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    ResponseEntity<AuthCodeResponse> getToken(
            @RequestBody MultiValueMap<String, String> formData
    );
}
