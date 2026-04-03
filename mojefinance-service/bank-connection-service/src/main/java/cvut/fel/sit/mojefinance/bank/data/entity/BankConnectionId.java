package cvut.fel.sit.mojefinance.bank.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class BankConnectionId {
    @Column(name = "principal_name", nullable = false)
    public String principalName;

    @Column(name = "client_registration_id", nullable = false)
    public String clientRegistrationId;
}
