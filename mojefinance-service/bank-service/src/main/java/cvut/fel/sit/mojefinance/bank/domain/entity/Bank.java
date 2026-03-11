package cvut.fel.sit.mojefinance.bank.domain.entity;

import lombok.Data;

@Data
public class Bank {
    private String name;
    private Boolean isFake;
    private BankConnectionStatus connectionStatus;
}
