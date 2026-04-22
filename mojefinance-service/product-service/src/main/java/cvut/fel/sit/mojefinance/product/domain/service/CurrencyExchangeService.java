package cvut.fel.sit.mojefinance.product.domain.service;

import cvut.fel.sit.mojefinance.product.domain.entity.Amount;

public interface CurrencyExchangeService {
    Amount exchangeAmount(Amount amount);
}
