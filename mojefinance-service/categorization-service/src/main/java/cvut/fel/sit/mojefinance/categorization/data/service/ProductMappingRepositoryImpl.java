package cvut.fel.sit.mojefinance.categorization.data.service;

import cvut.fel.sit.mojefinance.categorization.data.entity.ProductCategoryEntity;
import cvut.fel.sit.mojefinance.categorization.data.entity.ProductMappingEntity;
import cvut.fel.sit.mojefinance.categorization.data.repository.ProductCategoryJpaRepository;
import cvut.fel.sit.mojefinance.categorization.data.repository.ProductMappingJpaRepository;
import cvut.fel.sit.mojefinance.categorization.data.repository.ProductMappingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class ProductMappingRepositoryImpl implements ProductMappingRepository {
    private final ProductCategoryJpaRepository productCategoryJpaRepository;
    private final ProductMappingJpaRepository productMappingJpaRepository;

    @Override
    public void addProductMapping(ProductMappingEntity productMappingEntity) {
        productMappingJpaRepository.save(productMappingEntity);
    }

    @Override
    public List<ProductMappingEntity> getAllProductMappingsByProductNames(Set<String> productNames) {
        return productMappingJpaRepository.findAllByProductNameIn(productNames);
    }

    @Override
    public ProductCategoryEntity getProductCategoryByCategoryName(String categoryName) {
        return productCategoryJpaRepository.findByCategoryName(categoryName);
    }
}
