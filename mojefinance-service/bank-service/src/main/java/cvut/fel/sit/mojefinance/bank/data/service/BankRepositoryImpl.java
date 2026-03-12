package cvut.fel.sit.mojefinance.bank.data.service;

import cvut.fel.sit.mojefinance.bank.data.dto.GetConnectedBanksDataResponse;
import cvut.fel.sit.mojefinance.bank.data.entity.BankEntity;
import cvut.fel.sit.mojefinance.bank.data.entity.ConnectedBankId;
import cvut.fel.sit.mojefinance.bank.data.repository.BankJpaRepository;
import cvut.fel.sit.mojefinance.bank.data.repository.BankRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BankRepositoryImpl implements BankRepository {
    private final BankJpaRepository bankJpaRepository;

    @Override
    public GetConnectedBanksDataResponse getAllConnectedBanksByPrincipalName(String principalName) {
        List<BankEntity> bankEntities = bankJpaRepository.findAllById_PrincipalName(principalName);
        return GetConnectedBanksDataResponse.builder()
                .connectedBanks(bankEntities != null ? bankEntities : Collections.emptyList())
                .build();
    }

    @Override
    public void addConnectedBank(BankEntity bankEntity) {
        bankJpaRepository.save(bankEntity);
    }

    @Override
    public void removeConnectedBankByClientRegistrationIdAndPrincipalName(String clientRegistrationId, String principalName) {
        ConnectedBankId id = new ConnectedBankId();
        id.setClientRegistrationId(clientRegistrationId);
        id.setPrincipalName(principalName);
        bankJpaRepository.removeBankEntityById(id);
    }

    @Override
    public void updateConnectedBank(BankEntity bankEntity) {
        bankJpaRepository.save(bankEntity);
    }
}
