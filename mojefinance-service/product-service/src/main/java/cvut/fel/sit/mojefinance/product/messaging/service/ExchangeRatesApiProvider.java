package cvut.fel.sit.mojefinance.product.messaging.service;

import cvut.fel.sit.er.openapi.model.ExchangeRate;
import cvut.fel.sit.mojefinance.product.messaging.client.ExchangeRatesApiFeignClient;
import cvut.fel.sit.shared.exception.ServiceException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeRatesApiProvider {
    private final ExchangeRatesApiFeignClient exchangeRatesApiFeignClient;

    @Value("${external.api.exchange-rates.apikey}")
    private String exchangeRatesApiKey;

    public BigDecimal fetchExchangeRate(String currency) {
        String correlationId = UUID.randomUUID().toString();

        log.info("Fetching exchange rates from KB");
        try {
            ResponseEntity<ExchangeRate> responseEntity = exchangeRatesApiFeignClient.getExchangeRates(correlationId, exchangeRatesApiKey, currency);
            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.hasBody()) {
                return responseEntity.getBody().getCashBuy();
            }
            throw new ServiceException("Failed to fetch exchange rates from KB API. Status: " + responseEntity.getStatusCode());
        } catch (FeignException e) {
            throw new ServiceException("Error calling KB API: " + e.getMessage(), e);
        }
    }
}
