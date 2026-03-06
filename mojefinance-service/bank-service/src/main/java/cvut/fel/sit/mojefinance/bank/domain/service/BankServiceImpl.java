package cvut.fel.sit.mojefinance.bank.domain.service;

import cvut.fel.sit.mojefinance.bank.data.repository.BankConnectionRepository;
import cvut.fel.sit.mojefinance.bank.domain.dto.ConnectedBanksDataResponse;
import cvut.fel.sit.mojefinance.bank.domain.dto.ConnectedBanksDomainResponse;
import cvut.fel.sit.mojefinance.bank.domain.mapper.BankDomainMapper;
import cvut.fel.sit.mojefinance.external.api.gateway.messaging.airbank.service.AirBankAdapter;
import cvut.fel.sit.mojefinance.external.api.gateway.messaging.cs.service.CeskaSporitelnaAdapter;
import cvut.fel.sit.mojefinance.external.api.gateway.messaging.csob.service.CSOBAdapter;
import cvut.fel.sit.mojefinance.external.api.gateway.messaging.kb.service.KBAdapter;
import cvut.fel.sit.mojefinance.shared.JwtDecoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BankServiceImpl implements BankService {
    private final CeskaSporitelnaAdapter ceskaSporitelnaAdapter;
    private final CSOBAdapter csobAdapter;
    private final AirBankAdapter airBankAdapter;
    private final KBAdapter kbAdapter;
    private final BankConnectionRepository bankConnectionRepository;
    private final BankDomainMapper bankDomainMapper;

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
    public ConnectedBanksDomainResponse getConnectedBanks(String authorization) {
        String username = JwtDecoder.extractUsernameFromJWT(authorization);
        ConnectedBanksDataResponse dataResponse = bankConnectionRepository.getAllConnectedBanksByCustomerUsername(username);
        return bankDomainMapper.toConnectedBanksDomainResponse(dataResponse);
    }
}
