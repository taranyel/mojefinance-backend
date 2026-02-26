package cvut.fel.sit.mojefinance.bank.connection.domain.service;

import cvut.fel.sit.mojefinance.external.api.gateway.messaging.airbank.service.AirBankAdapter;
import cvut.fel.sit.mojefinance.external.api.gateway.messaging.cs.service.CeskaSporitelnaAdapter;
import cvut.fel.sit.mojefinance.external.api.gateway.messaging.csob.service.CSOBAdapter;
import cvut.fel.sit.mojefinance.external.api.gateway.messaging.kb.service.KBAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BankConnectionServiceImpl implements BankConnectionService {
    private final CeskaSporitelnaAdapter ceskaSporitelnaAdapter;
    private final CSOBAdapter csobAdapter;
    private final AirBankAdapter airBankAdapter;
    private final KBAdapter kbAdapter;

    @Override
    public void connectCeskaSporitelna(String code) {
        ceskaSporitelnaAdapter.connectCeskaSporitelna(code);
    }

    @Override
    public void connectCSOB(String code) {
        csobAdapter.connectCSOB(code);
    }

    @Override
    public void connectAirBank(String code) {
        airBankAdapter.connectAirBank(code);
    }

    @Override
    public void connectKB(String code) {
        kbAdapter.connectKB(code);
    }

    @Override
    public void connectReiffeisenBank(String code) {
    }
}
