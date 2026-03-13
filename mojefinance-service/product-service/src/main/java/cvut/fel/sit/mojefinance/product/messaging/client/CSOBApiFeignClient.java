package cvut.fel.sit.mojefinance.product.messaging.client;

import cvut.fel.sit.csob.openapi.model.GetAccountsRes;
import cvut.fel.sit.mojefinance.product.messaging.config.CsobFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "CSOBApiFeignClient",
        url = "${external.api.csob.base-url}",
        configuration = CsobFeignConfig.class
)
public interface CSOBApiFeignClient {

    @GetMapping(value = "${external.api.csob.accounts-path}")
    ResponseEntity<GetAccountsRes> getAccounts(
            @RequestHeader("Authorization") String authorization,
            @RequestHeader ("X-Request-ID") String xRequestId,
            @RequestHeader ("User-Involved") Boolean userInvolved,
            @RequestHeader ("TPP-Name") String tppName,
            @RequestHeader ("APIKEY") String apiKey,
            @RequestHeader ("Content-Type") String contentType
    );
}
