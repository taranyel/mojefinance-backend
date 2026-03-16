package cvut.fel.sit.mojefinance.product.domain.entity;

import lombok.AllArgsConstructor;import lombok.Builder;
import lombok.Data;import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Amount {
    private String currency;
    private BigDecimal value;
}
