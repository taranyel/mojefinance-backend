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

import cvut.fel.sit.mojefinance.categorization.data.entity.TransactionCategoryEntity;
import cvut.fel.sit.mojefinance.categorization.data.entity.TransactionMappingEntity;
import cvut.fel.sit.mojefinance.categorization.data.repository.TransactionMappingRepository;
import cvut.fel.sit.mojefinance.categorization.messaging.service.GeminiProvider;
import cvut.fel.sit.shared.entity.TransactionCategory;

import java.util.Set;
import java.util.List;
import java.util.Map;

class TransactionCategorizationHelperTest {
    @Mock
    private GeminiProvider geminiProvider;
    @Mock
    private TransactionMappingRepository transactionMappingRepository;
    @InjectMocks
    private TransactionCategorizationHelper helper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        helper = new TransactionCategorizationHelper(geminiProvider, transactionMappingRepository);
    }

    @Test
    void mapExistingTransactionMappings_mapsCorrectly() {
        TransactionCategoryEntity catEntity = TransactionCategoryEntity.builder().categoryName("GROCERIES").build();
        TransactionMappingEntity mapping = TransactionMappingEntity.builder().transactionName("Tesco").transactionCategory(catEntity).build();
        Map<String, TransactionCategory> result = helper.mapExistingTransactionMappings(List.of(mapping));
        assertEquals(1, result.size());
        assertEquals(TransactionCategory.GROCERIES, result.get("Tesco"));
    }

    @Test
    void filterUnmappedTransactions_filtersCorrectly() {
        Set<String> requested = Set.of("A", "B", "C");
        Map<String, TransactionCategory> existing = Map.of("A", TransactionCategory.OTHER_EXPENSE);
        Set<String> result = helper.filterUnmappedTransactions(requested, existing);
        assertEquals(Set.of("B", "C"), result);
    }

    @Test
    void getAllTransactionMappingsByTransactionNames_delegatesToRepository() {
        Set<String> names = Set.of("A", "B");
        List<TransactionMappingEntity> mappings = List.of(mock(TransactionMappingEntity.class));
        when(transactionMappingRepository.getAllTransactionMappingsByTransactionNames(names)).thenReturn(mappings);
        assertEquals(mappings, helper.getAllTransactionMappingsByTransactionNames(names));
    }

    @Test
    void saveNewTransactionMappings_savesAndReturnsMappings() {
        Set<String> unmapped = Set.of("Trans1");
        when(geminiProvider.askGemini(anyString())).thenReturn("GROCERIES");
        TransactionCategoryEntity catEntity = TransactionCategoryEntity.builder().categoryName("GROCERIES").build();
        when(transactionMappingRepository.getTransactionCategoryByCategoryName("GROCERIES")).thenReturn(catEntity);
        doNothing().when(transactionMappingRepository).addTransactionMapping(any());
        Map<String, TransactionCategory> result = helper.saveNewTransactionMappings(unmapped);
        assertEquals(1, result.size());
        assertEquals(TransactionCategory.GROCERIES, result.get("Trans1"));
        verify(transactionMappingRepository).addTransactionMapping(any());
    }

    @Test
    void saveNewTransactionMappings_handlesUnknownCategory() {
        Set<String> unmapped = Set.of("Trans2");
        when(geminiProvider.askGemini(anyString())).thenReturn("UNKNOWN_CATEGORY");
        TransactionCategoryEntity catEntity = TransactionCategoryEntity.builder().categoryName("UNCATEGORIZED").build();
        when(transactionMappingRepository.getTransactionCategoryByCategoryName("UNCATEGORIZED")).thenReturn(catEntity);
        doNothing().when(transactionMappingRepository).addTransactionMapping(any());
        Map<String, TransactionCategory> result = helper.saveNewTransactionMappings(unmapped);
        assertEquals(1, result.size());
        assertEquals(TransactionCategory.UNCATEGORIZED, result.get("Trans2"));
        verify(transactionMappingRepository).addTransactionMapping(any());
    }
}
