package cvut.fel.sit.mojefinance.external.api.gateway.data.service;
import cvut.fel.sit.mojefinance.external.api.gateway.data.dto.ConnectedBanksResponse;
import cvut.fel.sit.mojefinance.external.api.gateway.data.mapper.ConnectedBankMapper;
import cvut.fel.sit.mojefinance.external.api.gateway.data.repository.BankConnectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class BankConnectionService {
    private final BankConnectionRepository repository;
    private final ConnectedBankMapper mapper;

    public ConnectedBanksResponse getAllConnectedBanksByPrincipalName(String customerUsername) {
        List<String> connectedClients = repository.getConnectedBanks(customerUsername);
        return ConnectedBanksResponse.builder()
                .connectedBanks(mapper.mapClientRegistrationIdsToConnectedBanks(connectedClients))
                .build();
    }
}
