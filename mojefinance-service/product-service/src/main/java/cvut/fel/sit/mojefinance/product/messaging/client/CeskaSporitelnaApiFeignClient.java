package cvut.fel.sit.mojefinance.product.messaging.client;

import cvut.fel.sit.cs.openapi.model.MyAccountsGet200Response;
import cvut.fel.sit.mojefinance.product.messaging.config.CeskaSporitelnaFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "CeskaSporitelnaApiFeignClient",
        url = "${external.api.ceska-sporitelna.base-url}",
        configuration = CeskaSporitelnaFeignConfig.class
)
public interface CeskaSporitelnaApiFeignClient {

    @GetMapping(value = "${external.api.ceska-sporitelna.accounts-path}",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<MyAccountsGet200Response> getAccounts(
            @RequestHeader("Authorization") String authorization,
            @RequestHeader("WEB-API-key") String apiKey
    );
}
