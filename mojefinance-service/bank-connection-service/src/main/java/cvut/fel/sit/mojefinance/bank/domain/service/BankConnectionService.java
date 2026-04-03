package cvut.fel.sit.mojefinance.bank.domain.service;

import cvut.fel.sit.mojefinance.bank.domain.dto.ConnectBankDomainRequest;
import cvut.fel.sit.mojefinance.bank.domain.dto.ConnectedBanksDomainResponse;
import cvut.fel.sit.mojefinance.bank.domain.entity.BankConnection;

public interface BankConnectionService {
    BankConnection connectBank(ConnectBankDomainRequest request);

    void disconnectBank(String clientRegistrationId);

    ConnectedBanksDomainResponse getConnectedBanks();
}
