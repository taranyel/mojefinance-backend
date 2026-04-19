package cvut.fel.sit.mojefinance.categorization.data.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ProductMappingRepositoryImplTest {
    @Mock
    private cvut.fel.sit.mojefinance.categorization.data.repository.ProductCategoryJpaRepository productCategoryJpaRepository;
    @Mock
    private cvut.fel.sit.mojefinance.categorization.data.repository.ProductMappingJpaRepository productMappingJpaRepository;
    @InjectMocks
    private ProductMappingRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        repository = new ProductMappingRepositoryImpl(productCategoryJpaRepository, productMappingJpaRepository);
    }

    @Test
    void addProductMapping_savesEntity() {
        cvut.fel.sit.mojefinance.categorization.data.entity.ProductMappingEntity entity = mock(cvut.fel.sit.mojefinance.categorization.data.entity.ProductMappingEntity.class);
        repository.addProductMapping(entity);
        verify(productMappingJpaRepository).save(entity);
    }

    @Test
    void getAllProductMappingsByProductNames_returnsList() {
        Set<String> names = Set.of("A", "B");
        List<cvut.fel.sit.mojefinance.categorization.data.entity.ProductMappingEntity> entities = List.of(mock(cvut.fel.sit.mojefinance.categorization.data.entity.ProductMappingEntity.class));
        when(productMappingJpaRepository.findAllByProductNameIn(names)).thenReturn(entities);
        List<cvut.fel.sit.mojefinance.categorization.data.entity.ProductMappingEntity> result = repository.getAllProductMappingsByProductNames(names);
        assertEquals(entities, result);
    }

    @Test
    void getProductCategoryByCategoryName_returnsCategory() {
        String categoryName = "cat";
        cvut.fel.sit.mojefinance.categorization.data.entity.ProductCategoryEntity entity = mock(cvut.fel.sit.mojefinance.categorization.data.entity.ProductCategoryEntity.class);
        when(productCategoryJpaRepository.findByCategoryName(categoryName)).thenReturn(entity);
        assertEquals(entity, repository.getProductCategoryByCategoryName(categoryName));
    }
}
