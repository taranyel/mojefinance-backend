package cvut.fel.sit.mojefinance.bank.data.entity;

import jakarta.persistence.*;

import lombok.Data;

@Data
@Entity
@Table(name = "bank_connection")
public class BankConnectionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long bankConnectionId;

    @Column(nullable = false)
    private String customerUsername;

    @Column(nullable = false)
    private String bankName;

    private String refreshToken;

    @Column(nullable = false)
    private String bankConnectionStatus;
}
