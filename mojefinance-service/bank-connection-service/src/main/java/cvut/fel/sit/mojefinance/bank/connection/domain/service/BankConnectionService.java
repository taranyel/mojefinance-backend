package cvut.fel.sit.mojefinance.bank.connection.domain.service;

public interface BankConnectionService {
    void connectCeskaSporitelna(String code);

    void connectCSOB(String code);

    void connectAirBank(String code);

    void connectKB(String code);

    void connectReiffeisenBank(String code);
}
