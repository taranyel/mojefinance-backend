package cvut.fel.sit.mojefinance.external.api.gateway.messaging.kb.service;

import cvut.fel.sit.mojefinance.external.api.gateway.messaging.kb.client.KBApiFeignClient;
import cvut.fel.sit.mojefinance.external.api.gateway.util.Constants;
import cvut.fel.sit.mojefinance.external.api.gateway.messaging.util.ExchangeTokenHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class KBAdapterImpl implements KBAdapter {
    private final KBApiFeignClient kbApiFeignClient;
    private final ExchangeTokenHelper exchangeTokenHelper;

    @Override
    public void connectKB(String code) {
        exchangeTokenHelper.exchangeToken(Constants.KB_CLIENT_REGISTRATION_ID, code);
    }
}
