package cvut.fel.sit.mojefinance.external.api.gateway.messaging.reif.service;

import cvut.fel.sit.mojefinance.external.api.gateway.messaging.reif.client.ReiffeisenBankApiFeignClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReiffeisenBankAdapterImpl implements ReiffeisenBankAdapter {
    private final ReiffeisenBankApiFeignClient reiffeisenBankApiFeignClient;
}
