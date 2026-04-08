package cvut.fel.sit.mojefinance.categorization.data.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "product_mapping")
public class ProductMappingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productMappingId;

    @Column(nullable = false, unique = true)
    private String productName;

    @ManyToOne
    @JoinColumn(name = "product_category_id", nullable = false)
    private ProductCategoryEntity productCategory;
}
