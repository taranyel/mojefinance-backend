package cvut.fel.sit.mojefinance.product.messaging.client;

import cvut.fel.sit.airbank.openapi.model.AccountList;
import cvut.fel.sit.airbank.openapi.model.BalanceList;
import cvut.fel.sit.airbank.openapi.model.TransactionList;import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;import org.springframework.web.bind.annotation.RequestParam;

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

    @GetMapping(value = "${external.api.air-bank.balances-path}",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<BalanceList> getAccountBalance(
            @RequestHeader("Authorization") String authorization,
            @PathVariable String id
    );

    @GetMapping(value = "${external.api.air-bank.transactions-path}",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<TransactionList> getTransactions(
            @RequestHeader("Authorization") String authorization,
            @PathVariable String accountId,
            @RequestParam("fromDate") String fromDate,
            @RequestParam("toDate") String toDate
    );
}
