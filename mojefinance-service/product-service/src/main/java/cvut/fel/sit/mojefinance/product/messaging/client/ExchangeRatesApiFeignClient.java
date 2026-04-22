package cvut.fel.sit.mojefinance.product.messaging.client;

import cvut.fel.sit.er.openapi.model.ExchangeRate;
import cvut.fel.sit.mojefinance.product.messaging.config.KBFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "ExchangeRatesApiFeignClient",
        url = "${external.api.exchange-rates.base-url}",
        configuration = KBFeignConfig.class
)
public interface ExchangeRatesApiFeignClient {

    @GetMapping(value = "${external.api.exchange-rates.exchange-rates-path}",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ExchangeRate> getExchangeRates(
            @RequestHeader("x-correlation-id") String xCorrelationId,
            @RequestHeader("apiKey") String apiKey,
            @PathVariable String currency
    );
}
