package cvut.fel.sit.mojefinance.external.api.gateway.messaging.airbank.service;

import cvut.fel.sit.mojefinance.external.api.gateway.data.entity.ConnectedBank;
import cvut.fel.sit.mojefinance.external.api.gateway.messaging.airbank.client.AirBankApiFeignClient;
import cvut.fel.sit.mojefinance.external.api.gateway.util.Constants;
import cvut.fel.sit.mojefinance.external.api.gateway.messaging.util.ExchangeTokenHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AirBankAdapterImpl implements AirBankAdapter {
    private final AirBankApiFeignClient airBankApiFeignClient;
    private final ExchangeTokenHelper exchangeTokenHelper;

    @Override
    public ConnectedBank connectAirBank(String code) {
        exchangeTokenHelper.exchangeToken(Constants.AIR_BANK_CLIENT_REGISTRATION_ID, code);
        return ConnectedBank.builder()
                .name(Constants.AIR_BANK_NAME)
                .build();
    }
}
