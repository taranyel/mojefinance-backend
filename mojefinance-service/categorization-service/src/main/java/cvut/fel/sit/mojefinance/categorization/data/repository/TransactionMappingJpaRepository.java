package cvut.fel.sit.mojefinance.categorization.data.repository;

import cvut.fel.sit.mojefinance.categorization.data.entity.TransactionMappingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface TransactionMappingJpaRepository extends JpaRepository<TransactionMappingEntity, Long> {
    List<TransactionMappingEntity> findAllByTransactionNameIn(Set<String> transactionsNames);
}
