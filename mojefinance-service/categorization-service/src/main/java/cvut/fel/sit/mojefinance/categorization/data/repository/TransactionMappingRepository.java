package cvut.fel.sit.mojefinance.categorization.data.repository;


import cvut.fel.sit.mojefinance.categorization.data.entity.TransactionCategoryEntity;
import cvut.fel.sit.mojefinance.categorization.data.entity.TransactionMappingEntity;

import java.util.List;
import java.util.Set;

public interface TransactionMappingRepository {
    void addTransactionMapping(TransactionMappingEntity transactionMappingEntity);

    List<TransactionMappingEntity> getAllTransactionMappingsByTransactionNames(Set<String> transactionNames);

    TransactionCategoryEntity getTransactionCategoryByCategoryName(String categoryName);
}
