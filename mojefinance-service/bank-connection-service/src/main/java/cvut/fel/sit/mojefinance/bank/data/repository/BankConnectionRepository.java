package cvut.fel.sit.mojefinance.bank.data.repository;

import cvut.fel.sit.mojefinance.bank.data.entity.BankConnectionEntity;
import cvut.fel.sit.mojefinance.bank.domain.dto.ConnectedBanksResponse;


public interface BankConnectionRepository {
    ConnectedBanksResponse getAllConnectedBanksByPrincipalName(String principalName);

    void addConnectedBank(BankConnectionEntity bankConnectionEntity);

    void removeConnectedBankByClientRegistrationIdAndPrincipalName(String clientRegistrationId, String principalName);

    void updateConnectedBank(BankConnectionEntity bankConnectionEntity);
}
