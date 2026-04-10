package cvut.fel.sit.mojefinance.product.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupedProducts {
    private String groupName;
    private List<Product> products;
    private Amount totalAmount;
}
