package cvut.fel.sit.mojefinance.bank.data.repository;

import cvut.fel.sit.mojefinance.bank.data.dto.GetConnectedBanksDataResponse;
import cvut.fel.sit.mojefinance.bank.data.entity.BankEntity;


public interface BankRepository {
    GetConnectedBanksDataResponse getAllConnectedBanksByPrincipalName(String principalName);

    void addConnectedBank(BankEntity bankEntity);

    void removeConnectedBankByClientRegistrationIdAndPrincipalName(String clientRegistrationId, String principalName);

    void updateConnectedBank(BankEntity bankEntity);
}
