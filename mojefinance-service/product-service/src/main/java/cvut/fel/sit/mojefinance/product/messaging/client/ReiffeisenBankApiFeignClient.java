package cvut.fel.sit.mojefinance.product.messaging.client;

import cvut.fel.sit.mojefinance.product.messaging.config.ReiffeisenBankFeignConfig;
import cvut.fel.sit.reif.openapi.model.GetAccounts200Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "ReiffeisenBankApiFeignClient",
        url = "${external.api.reiffeisen-bank.base-url}",
        configuration = ReiffeisenBankFeignConfig.class
)
public interface ReiffeisenBankApiFeignClient {

    @GetMapping(value = "${external.api.reiffeisen-bank.accounts-path}",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<GetAccounts200Response> getAccounts(
            @RequestHeader ("X-IBM-Client-Id") String xIbmClientId,
            @RequestHeader ("X-Request-Id") String xRequestId
    );
}
