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

import cvut.fel.sit.mojefinance.categorization.data.entity.TransactionMappingEntity;
import cvut.fel.sit.mojefinance.categorization.data.entity.TransactionCategoryEntity;
import cvut.fel.sit.mojefinance.categorization.data.repository.TransactionMappingJpaRepository;
import cvut.fel.sit.mojefinance.categorization.data.repository.TransactionCategoryJpaRepository;

class TransactionMappingRepositoryImplTest {
    @Mock
    private TransactionMappingJpaRepository transactionMappingJpaRepository;
    @Mock
    private TransactionCategoryJpaRepository transactionCategoryJpaRepository;
    @InjectMocks
    private TransactionMappingRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        repository = new TransactionMappingRepositoryImpl(transactionMappingJpaRepository, transactionCategoryJpaRepository);
    }

    @Test
    void addTransactionMapping_savesEntity() {
        TransactionMappingEntity entity = mock(TransactionMappingEntity.class);
        repository.addTransactionMapping(entity);
        verify(transactionMappingJpaRepository).save(entity);
    }

    @Test
    void getAllTransactionMappingsByTransactionNames_returnsList() {
        Set<String> names = Set.of("A", "B");
        List<TransactionMappingEntity> entities = List.of(mock(TransactionMappingEntity.class));
        when(transactionMappingJpaRepository.findAllByTransactionNameIn(names)).thenReturn(entities);
        List<TransactionMappingEntity> result = repository.getAllTransactionMappingsByTransactionNames(names);
        assertEquals(entities, result);
    }

    @Test
    void getTransactionCategoryByCategoryName_returnsCategory() {
        String categoryName = "cat";
        TransactionCategoryEntity entity = mock(TransactionCategoryEntity.class);
        when(transactionCategoryJpaRepository.findByCategoryName(categoryName)).thenReturn(entity);
        assertEquals(entity, repository.getTransactionCategoryByCategoryName(categoryName));
    }
}
