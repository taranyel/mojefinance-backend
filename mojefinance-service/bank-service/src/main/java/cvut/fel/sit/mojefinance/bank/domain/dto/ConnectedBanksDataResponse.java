package cvut.fel.sit.mojefinance.bank.domain.dto;

import cvut.fel.sit.mojefinance.bank.data.entity.BankConnectionEntity;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ConnectedBanksDataResponse {
    private List<BankConnectionEntity> connectedBanks;
}
