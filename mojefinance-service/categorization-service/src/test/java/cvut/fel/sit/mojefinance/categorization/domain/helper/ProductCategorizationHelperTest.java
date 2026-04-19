package cvut.fel.sit.mojefinance.categorization.domain.helper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.any;
import cvut.fel.sit.mojefinance.categorization.data.entity.ProductCategoryEntity;
import cvut.fel.sit.mojefinance.categorization.data.entity.ProductMappingEntity;
import cvut.fel.sit.mojefinance.categorization.data.repository.ProductMappingRepository;
import cvut.fel.sit.mojefinance.categorization.messaging.service.GeminiProvider;
import cvut.fel.sit.shared.entity.ProductCategory;

import java.util.Set;
import java.util.List;
import java.util.Map;

class ProductCategorizationHelperTest {
    @Mock
    private ProductMappingRepository productMappingRepository;
    @Mock
    private GeminiProvider geminiProvider;
    @InjectMocks
    private ProductCategorizationHelper helper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        helper = new ProductCategorizationHelper(productMappingRepository, geminiProvider);
    }

    @Test
    void getAllProductMappingsByProductNames_delegatesToRepository() {
        Set<String> names = Set.of("A", "B");
        List<ProductMappingEntity> mappings = List.of(mock(ProductMappingEntity.class));
        when(productMappingRepository.getAllProductMappingsByProductNames(names)).thenReturn(mappings);
        assertEquals(mappings, helper.getAllProductMappingsByProductNames(names));
    }

    @Test
    void mapExistingProductMappings_mapsCorrectly() {
        ProductCategoryEntity catEntity = ProductCategoryEntity.builder().categoryName("LOAN").build();
        ProductMappingEntity mapping = ProductMappingEntity.builder().productName("LoanX").productCategory(catEntity).build();
        Map<String, ProductCategory> result = helper.mapExistingProductMappings(List.of(mapping));
        assertEquals(1, result.size());
        assertEquals(ProductCategory.LOAN, result.get("LoanX"));
    }

    @Test
    void filterUnmappedProducts_filtersCorrectly() {
        Set<String> requested = Set.of("A", "B", "C");
        Map<String, ProductCategory> existing = Map.of("A", ProductCategory.OTHER);
        Set<String> result = helper.filterUnmappedProducts(requested, existing);
        assertEquals(Set.of("B", "C"), result);
    }

    @Test
    void saveNewProductMappings_savesAndReturnsMappings() {
        Set<String> unmapped = Set.of("Prod1");
        when(geminiProvider.askGemini(anyString())).thenReturn("LOAN");
        ProductCategoryEntity catEntity = ProductCategoryEntity.builder().categoryName("LOAN").build();
        when(productMappingRepository.getProductCategoryByCategoryName("LOAN")).thenReturn(catEntity);
        doNothing().when(productMappingRepository).addProductMapping(any());
        Map<String, ProductCategory> result = helper.saveNewProductMappings(unmapped);
        assertEquals(1, result.size());
        assertEquals(ProductCategory.LOAN, result.get("Prod1"));
        verify(productMappingRepository).addProductMapping(any());
    }

    @Test
    void saveNewProductMappings_handlesUnknownCategory() {
        Set<String> unmapped = Set.of("Prod2");
        when(geminiProvider.askGemini(anyString())).thenReturn("UNKNOWN_CATEGORY");
        ProductCategoryEntity catEntity = ProductCategoryEntity.builder().categoryName("OTHER").build();
        when(productMappingRepository.getProductCategoryByCategoryName("OTHER")).thenReturn(catEntity);
        doNothing().when(productMappingRepository).addProductMapping(any());
        Map<String, ProductCategory> result = helper.saveNewProductMappings(unmapped);
        assertEquals(1, result.size());
        assertEquals(ProductCategory.OTHER, result.get("Prod2"));
        verify(productMappingRepository).addProductMapping(any());
    }
}
