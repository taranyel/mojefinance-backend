package cvut.fel.sit.mojefinance.product.messaging.client;

import cvut.fel.sit.kb.openapi.model.GeAccountTransactionsResponse;import cvut.fel.sit.kb.openapi.model.GetAccountBalanceResponse;
import cvut.fel.sit.kb.openapi.model.GetAccountListResponse;
import cvut.fel.sit.mojefinance.product.messaging.config.KBFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

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

    @GetMapping(value = "${external.api.kb.balances-path}",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<GetAccountBalanceResponse> getAccountBalance(
            @RequestHeader("Authorization") String authorization,
            @PathVariable String id
    );

    @GetMapping(value = "${external.api.kb.transactions-path}",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<GeAccountTransactionsResponse> getTransactions(
            @RequestHeader("Authorization") String authorization,
            @PathVariable String id,
            @RequestParam("fromDate") String fromDate,
            @RequestParam("toDate") String toDate
    );
}
