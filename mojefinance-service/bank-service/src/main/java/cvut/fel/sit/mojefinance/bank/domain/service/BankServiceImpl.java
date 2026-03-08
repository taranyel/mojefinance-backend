package cvut.fel.sit.mojefinance.bank.domain.service;

import cvut.fel.sit.mojefinance.bank.data.entity.BankConnectionEntity;
import cvut.fel.sit.mojefinance.bank.data.repository.BankConnectionRepository;
import cvut.fel.sit.mojefinance.bank.domain.dto.ConnectedBanksDataResponse;
import cvut.fel.sit.mojefinance.bank.domain.dto.ConnectedBanksDomainResponse;
import cvut.fel.sit.mojefinance.bank.domain.entity.BankConnectionStatus;
import cvut.fel.sit.mojefinance.bank.domain.mapper.BankDomainMapper;
import cvut.fel.sit.mojefinance.external.api.gateway.messaging.airbank.service.AirBankAdapter;
import cvut.fel.sit.mojefinance.external.api.gateway.messaging.cs.service.CeskaSporitelnaAdapter;
import cvut.fel.sit.mojefinance.external.api.gateway.messaging.csob.service.CSOBAdapter;
import cvut.fel.sit.mojefinance.external.api.gateway.messaging.kb.service.KBAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
        BankConnectionEntity bankConnection = BankConnectionEntity.builder()
                .bankName("Ceska Sporitelna")
                .bankConnectionStatus(BankConnectionStatus.CONNECTED.name())
                .build();
        bankConnectionRepository.connectNewBank(bankConnection);
    }

    @Override
    public void connectCSOB(String code) {
        log.info("Connecting to CSOB with code: {}", code);
        csobAdapter.connectCSOB(code);
        BankConnectionEntity bankConnection = BankConnectionEntity.builder()
                .bankName("CSOB")
                .bankConnectionStatus(BankConnectionStatus.CONNECTED.name())
                .build();
        bankConnectionRepository.connectNewBank(bankConnection);
    }

    @Override
    public void connectAirBank(String code) {
        log.info("Connecting to Air Bank with code: {}", code);
        airBankAdapter.connectAirBank(code);
        BankConnectionEntity bankConnection = BankConnectionEntity.builder()
                .bankName("Air Bank")
                .bankConnectionStatus(BankConnectionStatus.CONNECTED.name())
                .build();
        bankConnectionRepository.connectNewBank(bankConnection);
    }

    @Override
    public void connectKB(String code) {
        log.info("Connecting to KB with code: {}", code);
        kbAdapter.connectKB(code);
        BankConnectionEntity bankConnection = BankConnectionEntity.builder()
                .bankName("KB")
                .bankConnectionStatus(BankConnectionStatus.CONNECTED.name())
                .build();
        bankConnectionRepository.connectNewBank(bankConnection);
    }

    @Override
    public ConnectedBanksDomainResponse getConnectedBanks(String authorization) {
        log.info("Retrieving connected banks for authorized user.");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        ConnectedBanksDataResponse dataResponse = bankConnectionRepository.getAllConnectedBanksByCustomerUsername(username);
        log.info("Retrieved: {} connected banks.", dataResponse.getConnectedBanks().size());
        return bankDomainMapper.toConnectedBanksDomainResponse(dataResponse);
    }

    @Override
    public void connectReiffeisenBank() {
        BankConnectionEntity bankConnection = BankConnectionEntity.builder()
                .bankName("Reiffeisen Bank")
                .bankConnectionStatus(BankConnectionStatus.CONNECTED.name())
                .build();
        bankConnectionRepository.connectNewBank(bankConnection);
    }
}
