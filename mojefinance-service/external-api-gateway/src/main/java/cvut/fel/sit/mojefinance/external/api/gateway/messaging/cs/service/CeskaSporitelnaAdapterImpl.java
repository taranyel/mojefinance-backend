package cvut.fel.sit.mojefinance.external.api.gateway.messaging.cs.service;

import cvut.fel.sit.mojefinance.external.api.gateway.messaging.util.Constants;
import cvut.fel.sit.mojefinance.external.api.gateway.messaging.util.ExchangeTokenHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CeskaSporitelnaAdapterImpl implements CeskaSporitelnaAdapter {
    private final ExchangeTokenHelper exchangeTokenHelper;

    @Override
    public void connectCeskaSporitelna(String code) {
       exchangeTokenHelper.exchangeToken(Constants.CESKA_SPORITELNA_CLIENT_REGISTRATION_ID, code);
    }
}