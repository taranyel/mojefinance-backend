package cvut.fel.sit.mojefinance.product.domain.service;

import cvut.fel.sit.mojefinance.product.domain.entity.Amount;
import cvut.fel.sit.mojefinance.product.messaging.service.ExternalApiProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import static cvut.fel.sit.shared.util.Constants.CZK_CURRENCY_CODE;

@Slf4j
@Service
@RequiredArgsConstructor
public class CurrencyExchangeServiceImpl implements CurrencyExchangeService {
    private final ExternalApiProvider externalApiProvider;

    @Override
    public Amount exchangeAmount(Amount amount) {
        log.info("Exchanging amount: {}", amount);
        if (amount == null || amount.getValue() == null) {
            throw new IllegalArgumentException("Amount must not be null.");
        }
        BigDecimal exchangeRate = externalApiProvider.getExchangeRates(amount.getCurrency());
        BigDecimal originalAmount = amount.getValue();
        BigDecimal result = originalAmount.multiply(exchangeRate);
        log.info("Exchanged amount: {} {} to {} {}", originalAmount, amount.getCurrency(), result, CZK_CURRENCY_CODE);
        return Amount.builder()
                .value(result)
                .currency(CZK_CURRENCY_CODE)
                .build();
    }
}
