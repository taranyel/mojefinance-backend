package cvut.fel.sit.mojefinance.bank.domain.service;

import cvut.fel.sit.mojefinance.bank.domain.dto.ConnectedBanksDomainResponse;

public interface BankService {
    void connectCeskaSporitelna(String code);

    void connectCSOB(String code);

    void connectAirBank(String code);

    void connectKB(String code);

    ConnectedBanksDomainResponse getConnectedBanks(String authorization);
}
