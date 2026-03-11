package cvut.fel.sit.mojefinance.external.api.gateway.messaging.csob.service;

import cvut.fel.sit.mojefinance.external.api.gateway.data.entity.ConnectedBank;
import cvut.fel.sit.mojefinance.external.api.gateway.messaging.csob.client.CSOBApiFeignClient;
import cvut.fel.sit.mojefinance.external.api.gateway.util.Constants;
import cvut.fel.sit.mojefinance.external.api.gateway.messaging.util.ExchangeTokenHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class CSOBAdapterImpl implements CSOBAdapter {
    private final CSOBApiFeignClient csobApiFeignClient;
    private final ExchangeTokenHelper exchangeTokenHelper;

    @Override
    public ConnectedBank connectCSOB(String code) {
        exchangeTokenHelper.exchangeToken(Constants.CSOB_CLIENT_REGISTRATION_ID, code);
        return ConnectedBank.builder()
                .name(Constants.CSOB_NAME)
                .build();
    }
}
