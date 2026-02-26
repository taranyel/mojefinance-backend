package cvut.fel.sit.mojefinance.bank.connection.messaging.reif.client;

import cvut.fel.sit.mojefinance.bank.connection.messaging.reif.config.ReiffeisenBankFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(
        name = "ReiffeisenBankApiFeignClient",
        url = "${external.api.reiffeisen-bank.base-url}",
        configuration = ReiffeisenBankFeignConfig.class
)
public interface ReiffeisenBankApiFeignClient {


}
