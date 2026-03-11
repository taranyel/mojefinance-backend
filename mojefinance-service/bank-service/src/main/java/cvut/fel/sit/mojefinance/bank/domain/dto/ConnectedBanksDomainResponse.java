package cvut.fel.sit.mojefinance.bank.domain.dto;

import cvut.fel.sit.mojefinance.bank.domain.entity.Bank;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ConnectedBanksDomainResponse {
    private List<Bank> connectedBanks;
}
