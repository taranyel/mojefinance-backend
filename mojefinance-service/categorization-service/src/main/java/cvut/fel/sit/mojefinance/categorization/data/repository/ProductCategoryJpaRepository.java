package cvut.fel.sit.mojefinance.categorization.data.repository;

import cvut.fel.sit.mojefinance.categorization.data.entity.ProductCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductCategoryJpaRepository extends JpaRepository<ProductCategoryEntity, Long> {
    ProductCategoryEntity findByCategoryName(String categoryName);
}
