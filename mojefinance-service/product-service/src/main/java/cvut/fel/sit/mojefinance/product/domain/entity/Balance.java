package cvut.fel.sit.mojefinance.product.domain.entity;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class Balance {
    private String currency;
    private BigDecimal amount;
}
