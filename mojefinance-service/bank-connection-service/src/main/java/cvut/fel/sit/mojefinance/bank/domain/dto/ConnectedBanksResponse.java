package cvut.fel.sit.mojefinance.bank.domain.dto;

import cvut.fel.sit.mojefinance.bank.domain.entity.BankConnection;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ConnectedBanksResponse {
    private List<BankConnection> connectedBanks;
}
