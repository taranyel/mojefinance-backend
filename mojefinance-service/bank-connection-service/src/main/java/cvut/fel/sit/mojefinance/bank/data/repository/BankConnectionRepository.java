package cvut.fel.sit.mojefinance.bank.data.repository;

import cvut.fel.sit.mojefinance.bank.data.dto.ConnectedBanksDataResponse;
import cvut.fel.sit.mojefinance.bank.data.entity.BankConnectionEntity;


public interface BankConnectionRepository {
    ConnectedBanksDataResponse getAllConnectedBanksByPrincipalName(String principalName);

    void addConnectedBank(BankConnectionEntity bankConnectionEntity);

    void removeConnectedBankByClientRegistrationIdAndPrincipalName(String clientRegistrationId, String principalName);

    void updateConnectedBank(BankConnectionEntity bankConnectionEntity);
}
