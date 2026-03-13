package cvut.fel.sit.mojefinance.bank.domain.service;

import cvut.fel.sit.mojefinance.bank.domain.dto.ConnectBankDomainRequest;
import cvut.fel.sit.mojefinance.bank.domain.dto.ConnectedBanksDomainResponse;
import cvut.fel.sit.mojefinance.bank.domain.entity.BankDomainEntity;

public interface BankService {
    BankDomainEntity connectBank(ConnectBankDomainRequest request);

    void disconnectBank(String clientRegistrationId);

    ConnectedBanksDomainResponse getConnectedBanks();
}
