package cvut.fel.sit.mojefinance.product.domain.dto;

import cvut.fel.sit.mojefinance.product.domain.entity.GroupedProducts;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetsAndLiabilitiesResponse {
    private List<GroupedProducts> assets;
    private List<GroupedProducts> liabilities;
}
