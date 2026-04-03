package cvut.fel.sit.mojefinance.bank.data.service;

import cvut.fel.sit.mojefinance.bank.data.dto.ConnectedBanksDataResponse;
import cvut.fel.sit.mojefinance.bank.data.entity.BankConnectionEntity;
import cvut.fel.sit.mojefinance.bank.data.entity.BankConnectionId;
import cvut.fel.sit.mojefinance.bank.data.repository.BankConnectionJpaRepository;
import cvut.fel.sit.mojefinance.bank.data.repository.BankConnectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BankConnectionRepositoryImpl implements BankConnectionRepository {
    private final BankConnectionJpaRepository bankConnectionJpaRepository;

    @Override
    public ConnectedBanksDataResponse getAllConnectedBanksByPrincipalName(String principalName) {
        List<BankConnectionEntity> bankEntities = bankConnectionJpaRepository.findAllById_PrincipalName(principalName);
        return ConnectedBanksDataResponse.builder()
                .connectedBanks(bankEntities != null ? bankEntities : Collections.emptyList())
                .build();
    }

    @Override
    public void addConnectedBank(BankConnectionEntity bankConnectionEntity) {
        bankConnectionJpaRepository.save(bankConnectionEntity);
    }

    @Override
    public void removeConnectedBankByClientRegistrationIdAndPrincipalName(String clientRegistrationId, String principalName) {
        BankConnectionId id = new BankConnectionId();
        id.setClientRegistrationId(clientRegistrationId);
        id.setPrincipalName(principalName);
        bankConnectionJpaRepository.removeBankEntityById(id);
    }

    @Override
    public void updateConnectedBank(BankConnectionEntity bankConnectionEntity) {
        bankConnectionJpaRepository.save(bankConnectionEntity);
    }
}
