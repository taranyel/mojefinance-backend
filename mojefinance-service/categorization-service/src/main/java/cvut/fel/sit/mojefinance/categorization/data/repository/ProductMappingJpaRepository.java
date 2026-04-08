package cvut.fel.sit.mojefinance.categorization.data.repository;

import cvut.fel.sit.mojefinance.categorization.data.entity.ProductMappingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface ProductMappingJpaRepository extends JpaRepository<ProductMappingEntity, Long> {
    List<ProductMappingEntity> findAllByProductNameIn(Set<String> productNames);
}
