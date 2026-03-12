package cvut.fel.sit.mojefinance.bank.domain.entity;

import lombok.Data;

@Data
public class BankDomainEntity {
    private String bankName;
    private Boolean manuallyCreated;
    private BankConnectionStatus bankConnectionStatus;
    private String clientRegistrationId;
}
