package cvut.fel.sit.mojefinance.product.domain.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductIdentification {
    private String iban;
    private String productNumber;
}
