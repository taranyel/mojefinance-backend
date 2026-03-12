package cvut.fel.sit.mojefinance.bank.data.entity;

import jakarta.persistence.*;

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
    @EmbeddedId
    private ConnectedBankId id;

    @Column(nullable = false)
    private String bankName;

    @Column(nullable = false)
    private String bankConnectionStatus;

    @Column(nullable = false)
    private Boolean manuallyCreated;
}

