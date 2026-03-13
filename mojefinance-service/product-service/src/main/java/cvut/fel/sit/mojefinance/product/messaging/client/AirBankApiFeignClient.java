package cvut.fel.sit.mojefinance.product.messaging.client;

import cvut.fel.sit.airbank.openapi.model.AccountList;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "AirBankApiFeignClient",
        url = "${external.api.air-bank.base-url}"
)
public interface AirBankApiFeignClient {

    @GetMapping(value = "${external.api.air-bank.accounts-path}",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<AccountList> getAccounts(
            @RequestHeader("Authorization") String authorization
    );
}
