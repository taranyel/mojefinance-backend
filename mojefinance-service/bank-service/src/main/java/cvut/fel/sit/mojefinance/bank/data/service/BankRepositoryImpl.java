package cvut.fel.sit.mojefinance.bank.data.service;

import cvut.fel.sit.mojefinance.bank.data.dto.ConnectedBanksDataResponse;
import cvut.fel.sit.mojefinance.bank.data.entity.BankEntity;
import cvut.fel.sit.mojefinance.bank.data.repository.BankJpaRepository;
import cvut.fel.sit.mojefinance.bank.data.repository.BankRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BankRepositoryImpl implements BankRepository {
    private final BankJpaRepository bankJpaRepository;

    @Override
    public ConnectedBanksDataResponse getAllConnectedBanksByPrincipalName(String principalName) {
        List<BankEntity> bankEntities = bankJpaRepository.findAllByPrincipalName(principalName);
        return ConnectedBanksDataResponse.builder().connectedBanks(bankEntities).build();
    }

    @Override
    public void addConnectedBank(BankEntity bankEntity) {
        bankJpaRepository.save(bankEntity);
    }

    @Override
    public void removeConnectedBankById(Long bankId) {
        bankJpaRepository.deleteById(bankId);
    }

    @Override
    public void updateConnectedBankById(BankEntity bankEntity) {
        bankJpaRepository.save(bankEntity);
    }
}
