package cvut.fel.sit.mojefinance.bank.domain.dto;

import cvut.fel.sit.mojefinance.bank.domain.entity.BankDomainEntity;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConnectBankDomainRequest {
    private BankDomainEntity bankDomainEntity;
    private String code;
}
