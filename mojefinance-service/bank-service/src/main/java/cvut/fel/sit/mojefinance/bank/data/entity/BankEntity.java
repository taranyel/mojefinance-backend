package cvut.fel.sit.mojefinance.bank.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "connected_bank")
public class BankEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long connectedBankId;

    @Column(nullable = false)
    private String principalName;

    @Column(nullable = false)
    private String bankName;

    @Column(nullable = false)
    private String bankConnectionStatus;

    @Column(nullable = false)
    private Boolean isFake;

    @Override
    public String toString() {
        return "BankEntity{" +
                "bankName='" + bankName + '\'' +
                ", bankConnectionStatus='" + bankConnectionStatus + '\'' +
                ", isFake=" + isFake +
                '}';
    }
}
