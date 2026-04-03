package cvut.fel.sit.mojefinance.categorization.data.repository;


import cvut.fel.sit.mojefinance.categorization.data.entity.TransactionCategoryEntity;
import cvut.fel.sit.mojefinance.categorization.data.entity.TransactionMappingEntity;

import java.util.List;
import java.util.Set;

public interface TransactionMappingRepository {
    void addTransactionMapping(TransactionMappingEntity transactionMappingEntity);

    List<TransactionMappingEntity> getAllTransactionMappingsByTransactionsNames(Set<String> transactionsNames);

    TransactionCategoryEntity getTransactionCategoryByCategoryName(String categoryName);
}
