package cvut.fel.sit.mojefinance.external.api.gateway.messaging.airbank.client;

import cvut.fel.sit.cs.openapi.model.AuthCodeResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "AirBankApiFeignClient",
        url = "${external.api.air-bank.base-url}"
)
public interface AirBankApiFeignClient {

    @PostMapping(value = "${external.api.air-bank.token-path}",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    ResponseEntity<AuthCodeResponse> getToken(
            @RequestBody MultiValueMap<String, String> formData
    );
}
