package cvut.fel.sit.mojefinance.bank.domain.dto;

import cvut.fel.sit.mojefinance.bank.domain.entity.BankDomainEntity;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GetConnectedBanksDomainResponse {
    private List<BankDomainEntity> connectedBanks;
}
