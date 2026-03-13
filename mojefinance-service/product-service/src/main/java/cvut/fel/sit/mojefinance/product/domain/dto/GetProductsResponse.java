package cvut.fel.sit.mojefinance.product.domain.dto;

import cvut.fel.sit.mojefinance.product.domain.entity.Product;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GetProductsResponse {
    private List<Product> products;
}
