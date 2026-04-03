package cvut.fel.sit.mojefinance.categorization.data.service;

import cvut.fel.sit.mojefinance.categorization.data.entity.TransactionCategoryEntity;
import cvut.fel.sit.mojefinance.categorization.data.entity.TransactionMappingEntity;
import cvut.fel.sit.mojefinance.categorization.data.repository.TransactionCategoryJpaRepository;
import cvut.fel.sit.mojefinance.categorization.data.repository.TransactionMappingJpaRepository;
import cvut.fel.sit.mojefinance.categorization.data.repository.TransactionMappingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class TransactionMappingRepositoryImpl implements TransactionMappingRepository {
    private final TransactionMappingJpaRepository transactionMappingJpaRepository;
    private final TransactionCategoryJpaRepository transactionCategoryJpaRepository;

    @Override
    public void addTransactionMapping(TransactionMappingEntity transactionMappingEntity) {
        transactionMappingJpaRepository.save(transactionMappingEntity);
    }

    @Override
    public List<TransactionMappingEntity> getAllTransactionMappingsByTransactionsNames(Set<String> transactionsNames) {
        return transactionMappingJpaRepository.findAllByTransactionNameIn(transactionsNames);
    }

    @Override
    public TransactionCategoryEntity getTransactionCategoryByCategoryName(String categoryName) {
        return transactionCategoryJpaRepository.findByCategoryName(categoryName);
    }
}
