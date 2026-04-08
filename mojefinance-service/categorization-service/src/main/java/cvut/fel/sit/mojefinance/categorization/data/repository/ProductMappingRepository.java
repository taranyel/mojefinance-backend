package cvut.fel.sit.mojefinance.categorization.data.repository;


import cvut.fel.sit.mojefinance.categorization.data.entity.ProductCategoryEntity;
import cvut.fel.sit.mojefinance.categorization.data.entity.ProductMappingEntity;

import java.util.List;
import java.util.Set;

public interface ProductMappingRepository {
    void addProductMapping(ProductMappingEntity productMappingEntity);

    List<ProductMappingEntity> getAllProductMappingsByProductNames(Set<String> productNames);

    ProductCategoryEntity getProductCategoryByCategoryName(String categoryName);
}
