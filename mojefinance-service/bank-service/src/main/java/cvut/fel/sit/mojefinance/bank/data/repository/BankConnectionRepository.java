package cvut.fel.sit.mojefinance.bank.data.repository;

import cvut.fel.sit.mojefinance.bank.domain.dto.ConnectedBanksDataResponse;
import org.springframework.stereotype.Repository;


@Repository
public interface BankConnectionRepository {
    ConnectedBanksDataResponse getAllConnectedBanksByCustomerUsername(String customerUsername);
}
