package cvut.fel.sit.mojefinance.bank.data.service;

import cvut.fel.sit.mojefinance.bank.data.repository.BankConnectionJpaRepository;
import cvut.fel.sit.mojefinance.bank.data.repository.BankConnectionRepository;
import cvut.fel.sit.mojefinance.bank.domain.dto.ConnectedBanksDataResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class BankConnectionRepositoryImpl implements BankConnectionRepository {
    private final BankConnectionJpaRepository bankConnectionJpaRepository;

    @Override
    public ConnectedBanksDataResponse getAllConnectedBanksByCustomerUsername(String customerUsername) {
        return ConnectedBanksDataResponse.builder()
                .connectedBanks(bankConnectionJpaRepository.findAllByCustomerUsername(customerUsername))
                .build();
    }
}
