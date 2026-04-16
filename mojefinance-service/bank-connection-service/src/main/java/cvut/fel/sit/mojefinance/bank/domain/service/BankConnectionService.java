package cvut.fel.sit.mojefinance.bank.domain.service;

import cvut.fel.sit.mojefinance.bank.domain.dto.ConnectBankDomainRequest;
import cvut.fel.sit.mojefinance.bank.domain.dto.ConnectedBanksResponse;

public interface BankConnectionService {
    void connectBank(ConnectBankDomainRequest request);

    void disconnectBank(String clientRegistrationId);

    ConnectedBanksResponse getConnectedBanks();
}
