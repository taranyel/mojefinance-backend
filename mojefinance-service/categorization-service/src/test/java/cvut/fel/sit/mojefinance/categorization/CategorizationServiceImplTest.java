package cvut.fel.sit.mojefinance.categorization;

import cvut.fel.sit.mojefinance.categorization.domain.dto.CategorizeProductsRequest;
import cvut.fel.sit.mojefinance.categorization.domain.dto.CategorizeProductsResponse;
import cvut.fel.sit.mojefinance.categorization.domain.dto.CategorizeTransactionsRequest;
import cvut.fel.sit.mojefinance.categorization.domain.dto.CategorizeTransactionsResponse;
import cvut.fel.sit.mojefinance.categorization.data.entity.ProductMappingEntity;
import cvut.fel.sit.mojefinance.categorization.data.entity.TransactionMappingEntity;
import cvut.fel.sit.mojefinance.categorization.domain.helper.ProductCategorizationHelper;
import cvut.fel.sit.mojefinance.categorization.domain.helper.TransactionCategorizationHelper;
import cvut.fel.sit.shared.entity.ProductCategory;
import cvut.fel.sit.shared.entity.TransactionCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Set;
import java.util.Map;
import java.util.List;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

class CategorizationServiceImplTest {
    @Mock
    private TransactionCategorizationHelper transactionCategorizationHelper;
    @Mock
    private ProductCategorizationHelper productCategorizationHelper;
    @InjectMocks
    private CategorizationServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new CategorizationServiceImpl(transactionCategorizationHelper, productCategorizationHelper);
    }

    @Test
    void categorizeTransactions_returnsExpectedResponse() {
        Set<String> names = Set.of("A", "B");
        CategorizeTransactionsRequest req = CategorizeTransactionsRequest.builder().transactionNames(names).build();
        List<TransactionMappingEntity> mappings = List.of(mock(TransactionMappingEntity.class));
        Map<String, TransactionCategory> existing = new HashMap<>(Map.of("A", TransactionCategory.GROCERIES));
        Set<String> unmapped = Set.of("B");
        Map<String, TransactionCategory> saved = Map.of("B", TransactionCategory.OTHER_EXPENSE);
        when(transactionCategorizationHelper.getAllTransactionMappingsByTransactionNames(names)).thenReturn(mappings);
        when(transactionCategorizationHelper.mapExistingTransactionMappings(mappings)).thenReturn(existing);
        when(transactionCategorizationHelper.filterUnmappedTransactions(names, existing)).thenReturn(unmapped);
        when(transactionCategorizationHelper.saveNewTransactionMappings(unmapped)).thenReturn(saved);
        CategorizeTransactionsResponse resp = service.categorizeTransactions(req);
        assertEquals(2, resp.getCategorizedTransactions().size());
        assertEquals(TransactionCategory.GROCERIES, resp.getCategorizedTransactions().get("A"));
        assertEquals(TransactionCategory.OTHER_EXPENSE, resp.getCategorizedTransactions().get("B"));
    }

    @Test
    void categorizeProducts_returnsExpectedResponse() {
        Set<String> names = Set.of("X", "Y");
        CategorizeProductsRequest req = CategorizeProductsRequest.builder().productNames(names).build();
        List<ProductMappingEntity> mappings = List.of(mock(ProductMappingEntity.class));
        Map<String, ProductCategory> existing = new HashMap<>(Map.of("X", ProductCategory.LOAN));
        Set<String> unmapped = Set.of("Y");
        Map<String, ProductCategory> saved = Map.of("Y", ProductCategory.OTHER);
        when(productCategorizationHelper.getAllProductMappingsByProductNames(names)).thenReturn(mappings);
        when(productCategorizationHelper.mapExistingProductMappings(mappings)).thenReturn(existing);
        when(productCategorizationHelper.filterUnmappedProducts(names, existing)).thenReturn(unmapped);
        when(productCategorizationHelper.saveNewProductMappings(unmapped)).thenReturn(saved);
        CategorizeProductsResponse resp = service.categorizeProducts(req);
        assertEquals(2, resp.getCategorizedProducts().size());
        assertEquals(ProductCategory.LOAN, resp.getCategorizedProducts().get("X"));
        assertEquals(ProductCategory.OTHER, resp.getCategorizedProducts().get("Y"));
    }
}
