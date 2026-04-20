package cvut.fel.sit.mojefinance.budget.data.repository;

import cvut.fel.sit.mojefinance.budget.data.entity.BudgetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BudgetJpaRepository extends JpaRepository<BudgetEntity, Long> {
    List<BudgetEntity> findAllByPrincipalName(String principalName);
}
