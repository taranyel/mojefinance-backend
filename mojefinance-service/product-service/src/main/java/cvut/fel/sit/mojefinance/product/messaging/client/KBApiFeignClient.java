package cvut.fel.sit.mojefinance.product.messaging.client;

import cvut.fel.sit.kb.openapi.model.GetAccountListResponse;
import cvut.fel.sit.mojefinance.product.messaging.config.KBFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "KBApiFeignClient",
        url = "${external.api.kb.base-url}",
        configuration = KBFeignConfig.class
)
public interface KBApiFeignClient {

    @GetMapping(value = "${external.api.kb.accounts-path}",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<GetAccountListResponse> getAccounts(
            @RequestHeader("Authorization") String authorization
    );
}
