package cvut.fel.sit.mojefinance.bank.domain.dto;

import cvut.fel.sit.mojefinance.bank.domain.entity.BankConnection;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConnectBankDomainRequest {
    private BankConnection bankConnection;
    private String code;
}
