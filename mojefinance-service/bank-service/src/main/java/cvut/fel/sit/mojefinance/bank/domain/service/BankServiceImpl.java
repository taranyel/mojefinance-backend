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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
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
        log.info("Connecting to Ceska Sporitelna with code: {}", code);
        ceskaSporitelnaAdapter.connectCeskaSporitelna(code);
    }

    @Override
    public void connectCSOB(String code) {
        log.info("Connecting to CSOB with code: {}", code);
        csobAdapter.connectCSOB(code);
    }

    @Override
    public void connectAirBank(String code) {
        log.info("Connecting to Air Bank with code: {}", code);
        airBankAdapter.connectAirBank(code);
    }

    @Override
    public void connectKB(String code) {
        log.info("Connecting to KB with code: {}", code);
        kbAdapter.connectKB(code);
    }

    @Override
    public ConnectedBanksDomainResponse getConnectedBanks(String authorization) {
        log.info("Retrieving connected banks for authorized user.");
        String username = JwtDecoder.extractUsernameFromJWT(authorization);
        ConnectedBanksDataResponse dataResponse = bankConnectionRepository.getAllConnectedBanksByCustomerUsername(username);
        log.info("Retrieved: {} connected banks.", dataResponse.getConnectedBanks().size());
        return bankDomainMapper.toConnectedBanksDomainResponse(dataResponse);
    }
}
