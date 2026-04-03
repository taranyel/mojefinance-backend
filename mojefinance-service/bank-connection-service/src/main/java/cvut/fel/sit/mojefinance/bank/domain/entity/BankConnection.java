package cvut.fel.sit.mojefinance.bank.domain.entity;

import lombok.Data;

@Data
public class BankConnection {
    private String bankName;
    private Boolean manuallyCreated;
    private BankConnectionStatus bankConnectionStatus;
    private String clientRegistrationId;
}
