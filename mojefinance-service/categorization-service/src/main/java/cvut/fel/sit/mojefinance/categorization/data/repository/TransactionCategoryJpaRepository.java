package cvut.fel.sit.mojefinance.categorization.data.repository;

import cvut.fel.sit.mojefinance.categorization.data.entity.TransactionCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionCategoryJpaRepository extends JpaRepository<TransactionCategoryEntity, Long> {
    TransactionCategoryEntity findByCategoryName(String categoryName);
}
