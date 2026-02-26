package cvut.fel.sit.mojefinance.bank.connection.messaging.reif.service;

import cvut.fel.sit.mojefinance.bank.connection.messaging.reif.client.ReiffeisenBankApiFeignClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReiffeisenBankAdapterImpl implements ReiffeisenBankAdapter {
    private final ReiffeisenBankApiFeignClient reiffeisenBankApiFeignClient;

    @Value("${external.oauth2.reiffeisen-bank.client-id}")
    private String clientId;

}
