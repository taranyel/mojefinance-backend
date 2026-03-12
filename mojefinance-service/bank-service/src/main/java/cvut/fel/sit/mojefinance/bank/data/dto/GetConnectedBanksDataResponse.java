package cvut.fel.sit.mojefinance.bank.data.dto;

import cvut.fel.sit.mojefinance.bank.data.entity.BankEntity;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GetConnectedBanksDataResponse {
    private List<BankEntity> connectedBanks;
}

