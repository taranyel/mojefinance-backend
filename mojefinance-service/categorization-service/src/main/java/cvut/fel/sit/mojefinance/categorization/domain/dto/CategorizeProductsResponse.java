package cvut.fel.sit.mojefinance.categorization.domain.dto;

import cvut.fel.sit.mojefinance.categorization.domain.entity.ProductCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategorizeProductsResponse {
    private Map<String, ProductCategory> categorizedProducts;
}
