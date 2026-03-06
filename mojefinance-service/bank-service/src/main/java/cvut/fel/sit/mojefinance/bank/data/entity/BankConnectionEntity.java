package cvut.fel.sit.mojefinance.bank.data.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.GenerationType;

import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "bank_connection")
public class BankConnectionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long bankConnectionId;

    private String customerUsername;
    private String bankName;
    private String refreshToken;
    private String bankConnectionStatus;
}
