package cvut.fel.sit.mojefinance.bank.data.repository;

import cvut.fel.sit.mojefinance.bank.data.entity.BankConnectionEntity;
import cvut.fel.sit.mojefinance.bank.data.entity.BankConnectionId;
import cvut.fel.sit.mojefinance.bank.data.mapper.BankConnectionDataMapper;
import cvut.fel.sit.mojefinance.bank.domain.dto.ConnectedBanksResponse;
import cvut.fel.sit.mojefinance.bank.domain.entity.BankConnection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BankConnectionRepositoryImpl implements BankConnectionRepository {
    private final BankConnectionJpaRepository bankConnectionJpaRepository;
    private final BankConnectionDataMapper mapper;

    @Override
    public ConnectedBanksResponse getAllConnectedBanksByPrincipalName(String principalName) {
        List<BankConnectionEntity> bankEntities = bankConnectionJpaRepository.findAllById_PrincipalName(principalName);
        List<BankConnection> bankConnections = bankEntities.stream()
                .map(mapper::toBankConnection)
                .toList();
        return ConnectedBanksResponse.builder()
                .connectedBanks(bankConnections)
                .build();
    }

    @Override
    public void addConnectedBank(BankConnectionEntity bankConnectionEntity) {
        bankConnectionJpaRepository.save(bankConnectionEntity);
    }

    @Override
    public void removeConnectedBankByClientRegistrationIdAndPrincipalName(String clientRegistrationId, String principalName) {
        BankConnectionId id = new BankConnectionId();
        id.setClientRegistrationId(clientRegistrationId);
        id.setPrincipalName(principalName);
        bankConnectionJpaRepository.removeBankEntityById(id);
    }

    @Override
    public void updateConnectedBank(BankConnectionEntity bankConnectionEntity) {
        bankConnectionJpaRepository.save(bankConnectionEntity);
    }
}
