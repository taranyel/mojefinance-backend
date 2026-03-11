package cvut.fel.sit.mojefinance.bank.domain.service;

import cvut.fel.sit.mojefinance.bank.data.dto.ConnectedBanksDataResponse;
import cvut.fel.sit.mojefinance.bank.data.entity.BankEntity;
import cvut.fel.sit.mojefinance.bank.data.repository.BankRepository;
import cvut.fel.sit.mojefinance.bank.domain.dto.ConnectedBanksDomainResponse;
import cvut.fel.sit.mojefinance.bank.domain.entity.BankConnectionStatus;
import cvut.fel.sit.mojefinance.bank.domain.mapper.BankDomainMapper;
import cvut.fel.sit.mojefinance.external.api.gateway.data.dto.ConnectedBanksResponse;
import cvut.fel.sit.mojefinance.external.api.gateway.data.entity.ConnectedBank;
import cvut.fel.sit.mojefinance.external.api.gateway.data.service.BankConnectionService;
import cvut.fel.sit.mojefinance.external.api.gateway.messaging.airbank.service.AirBankAdapter;
import cvut.fel.sit.mojefinance.external.api.gateway.messaging.cs.service.CeskaSporitelnaAdapter;
import cvut.fel.sit.mojefinance.external.api.gateway.messaging.csob.service.CSOBAdapter;
import cvut.fel.sit.mojefinance.external.api.gateway.messaging.kb.service.KBAdapter;
import cvut.fel.sit.mojefinance.external.api.gateway.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BankServiceImpl implements BankService {
    private final CeskaSporitelnaAdapter ceskaSporitelnaAdapter;
    private final CSOBAdapter csobAdapter;
    private final AirBankAdapter airBankAdapter;
    private final KBAdapter kbAdapter;
    private final BankDomainMapper bankDomainMapper;
    private final BankConnectionService bankConnectionService;
    private final BankRepository bankRepository;

    @Override
    public void connectCeskaSporitelna(String code) {
        log.info("Connecting to Ceska Sporitelna with code: {}", code);
        ConnectedBank connectedBank = ceskaSporitelnaAdapter.connectCeskaSporitelna(code);
        BankEntity bankEntity = getBankEntity(connectedBank.getName());
        bankRepository.addConnectedBank(bankEntity);
    }

    @Override
    public void connectCSOB(String code) {
        log.info("Connecting to CSOB with code: {}", code);
        ConnectedBank connectedBank = csobAdapter.connectCSOB(code);
        BankEntity bankEntity = getBankEntity(connectedBank.getName());
        bankRepository.addConnectedBank(bankEntity);
    }

    @Override
    public void connectAirBank(String code) {
        log.info("Connecting to Air Bank with code: {}", code);
        ConnectedBank connectedBank = airBankAdapter.connectAirBank(code);
        BankEntity bankEntity = getBankEntity(connectedBank.getName());
        bankRepository.addConnectedBank(bankEntity);
    }

    @Override
    public void connectKB(String code) {
        log.info("Connecting to KB with code: {}", code);
        ConnectedBank connectedBank = kbAdapter.connectKB(code);
        BankEntity bankEntity = getBankEntity(connectedBank.getName());
        bankRepository.addConnectedBank(bankEntity);
    }

    @Override
    public void connectReiffeisenBank() {
        log.info("Connecting to Reiffeisen Bank.");
        BankEntity bankEntity = getBankEntity(Constants.REIFFEISENBANK_NAME);
        bankRepository.addConnectedBank(bankEntity);
    }

    @Override
    public ConnectedBanksDomainResponse getConnectedBanks() {
        log.info("Retrieving connected banks for authorized user.");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String principalName = authentication.getName();

        ConnectedBanksResponse externalResponse = bankConnectionService.getAllConnectedBanksByPrincipalName(principalName);
        ConnectedBanksDataResponse internalResponse = bankRepository.getAllConnectedBanksByPrincipalName(principalName);
        List<BankEntity> realConnectedBanks = internalResponse.getConnectedBanks().stream()
                .filter(bankEntity -> !bankEntity.getIsFake())
                .toList();

        for (BankEntity connectedBank : realConnectedBanks) {
            if (externalResponse.getConnectedBanks().stream()
                    .noneMatch(b -> !connectedBank.getIsFake() && b.getName().equals(connectedBank.getBankName()))
            ) {
                connectedBank.setBankConnectionStatus(BankConnectionStatus.DISCONNECTED.name());
                bankRepository.updateConnectedBankById(connectedBank);
            }
        }

        log.info("Connected banks: {}", internalResponse.getConnectedBanks());
        return bankDomainMapper.toConnectedBanksDomainResponse(internalResponse);
    }

    private static BankEntity getBankEntity(String connectedBank) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return BankEntity.builder()
                .principalName(authentication.getName())
                .bankName(connectedBank)
                .bankConnectionStatus(BankConnectionStatus.CONNECTED.name())
                .isFake(false)
                .build();
    }
}
