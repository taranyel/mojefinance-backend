package cvut.fel.sit.mojefinance.product.data.repository;

import cvut.fel.sit.mojefinance.product.data.entity.BudgetEntity;
import cvut.fel.sit.mojefinance.product.data.mapper.BudgetDataMapper;
import cvut.fel.sit.mojefinance.product.domain.dto.BudgetsResponse;
import cvut.fel.sit.mojefinance.product.domain.entity.Budget;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class BudgetRepositoryImpl implements BudgetRepository {
    private final BudgetJpaRepository budgetJpaRepository;
    private final BudgetDataMapper budgetDataMapper;

    @Override
    public void saveBudget(BudgetEntity budgetEntity) {
        budgetJpaRepository.save(budgetEntity);
    }

    @Override
    public BudgetsResponse getBudgets(String principalName) {
        List<BudgetEntity> budgetEntities = budgetJpaRepository.findAllByPrincipalName(principalName);
        List<Budget> budgets = budgetEntities.stream()
                .map(budgetDataMapper::toBudget)
                .toList();
        return BudgetsResponse.builder()
                .budgets(budgets)
                .build();
    }

    @Override
    public void deleteBudget(Long budgetId) {
        budgetJpaRepository.deleteById(budgetId);
    }

    @Override
    public Budget getBudgetById(Long budgetId) {
        return budgetJpaRepository.findById(budgetId)
                .map(budgetDataMapper::toBudget)
                .orElse(null);
    }
}
