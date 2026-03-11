package cvut.fel.sit.mojefinance.external.api.gateway.messaging.reif.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(
        name = "ReiffeisenBankApiFeignClient",
        url = "${external.api.reiffeisen-bank.base-url}"
)
public interface ReiffeisenBankApiFeignClient {


}
