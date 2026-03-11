package cvut.fel.sit.mojefinance.external.api.gateway.messaging.csob.client;

import cvut.fel.sit.cs.openapi.model.AuthCodeResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "CSOBApiFeignClient",
        url = "${external.api.csob.base-url}"
)
public interface CSOBApiFeignClient {

    @PostMapping(value = "${external.api.csob.token-path}",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    ResponseEntity<AuthCodeResponse> getToken(
            @RequestBody MultiValueMap<String, String> formData
    );
}
