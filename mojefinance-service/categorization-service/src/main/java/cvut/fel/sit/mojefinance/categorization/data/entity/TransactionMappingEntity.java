package cvut.fel.sit.mojefinance.categorization.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "transaction_mapping")
public class TransactionMappingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionMappingId;

    @Column(nullable = false, unique = true)
    private String transactionName;

    @ManyToOne
    @JoinColumn(name = "transaction_category_id", nullable = false)
    private TransactionCategoryEntity transactionCategory;
}
