package cvut.fel.sit.mojefinance.bank.data.repository;

import cvut.fel.sit.mojefinance.bank.data.dto.ConnectedBanksDataResponse;
import cvut.fel.sit.mojefinance.bank.data.entity.BankEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface BankRepository {
    ConnectedBanksDataResponse getAllConnectedBanksByPrincipalName(String principalName);

    void addConnectedBank(BankEntity bankEntity);

    void removeConnectedBankById(Long bankId);

    void updateConnectedBankById(BankEntity bankEntity);
}
