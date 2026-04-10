package cvut.fel.sit.mojefinance.product.domain.entity;

import cvut.fel.sit.shared.util.entity.ProductCategory;
import lombok.AllArgsConstructor;import lombok.Builder;
import lombok.Data;import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    private String productId;
    private String accountName;
    private ProductIdentification productIdentification;
    private String currency;
    private List<String> ownersNames;
    private ProductCategory productCategory;
    private String productName;
    private Amount balance;
    private Boolean manuallyCreated;
    private String bankCode;
    private BankDetails bankDetails;
}
