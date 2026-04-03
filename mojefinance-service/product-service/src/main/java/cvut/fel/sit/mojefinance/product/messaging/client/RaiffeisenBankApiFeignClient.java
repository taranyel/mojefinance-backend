package cvut.fel.sit.mojefinance.product.messaging.client;

import cvut.fel.sit.mojefinance.product.messaging.config.RaiffeisenBankFeignConfig;
import cvut.fel.sit.reif.openapi.model.GetAccounts200Response;
import cvut.fel.sit.reif.openapi.model.GetBalance200Response;
import cvut.fel.sit.reif.openapi.model.GetTransactionList200Response;import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "RaiffeisenBankApiFeignClient",
        url = "${external.api.raiffeisen-bank.base-url}",
        configuration = RaiffeisenBankFeignConfig.class
)
public interface RaiffeisenBankApiFeignClient {

    @GetMapping(value = "${external.api.raiffeisen-bank.accounts-path}",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<GetAccounts200Response> getAccounts(
            @RequestHeader("X-IBM-Client-Id") String xIbmClientId,
            @RequestHeader("X-Request-Id") String xRequestId
    );

    @GetMapping(value = "${external.api.raiffeisen-bank.balances-path}",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<GetBalance200Response> getAccountBalance(
            @RequestHeader("X-IBM-Client-Id") String xIbmClientId,
            @RequestHeader("X-Request-Id") String xRequestId,
            @PathVariable String accountNumber
    );

    @GetMapping(value = "${external.api.raiffeisen-bank.transactions-path}",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<GetTransactionList200Response> getTransactions(
            @RequestHeader("X-IBM-Client-Id") String xIbmClientId,
            @RequestHeader("X-Request-Id") String xRequestId,
            @PathVariable String accountNumber,
            @PathVariable String currencyCode,
            @RequestParam("from") String from,
            @RequestParam("to") String to
    );
}
