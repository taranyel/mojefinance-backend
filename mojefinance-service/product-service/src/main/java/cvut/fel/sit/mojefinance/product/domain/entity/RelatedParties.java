package cvut.fel.sit.mojefinance.product.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelatedParties {
    private String debtorName;
    private String creditorName;
    private String debtorAccountIban;
    private String creditorAccountIban;
}
