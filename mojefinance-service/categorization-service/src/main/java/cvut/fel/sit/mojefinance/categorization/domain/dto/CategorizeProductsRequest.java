package cvut.fel.sit.mojefinance.categorization.domain.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class CategorizeProductsRequest {
    private Set<String> productNames;
}
