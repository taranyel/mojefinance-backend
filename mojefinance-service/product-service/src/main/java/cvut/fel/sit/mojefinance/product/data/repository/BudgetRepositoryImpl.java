package cvut.fel.sit.mojefinance.product.data.repository;

import cvut.fel.sit.mojefinance.product.data.entity.BudgetEntity;
import cvut.fel.sit.mojefinance.product.data.mapper.BudgetDataMapper;
import cvut.fel.sit.mojefinance.product.domain.dto.BudgetsResponse;
import cvut.fel.sit.mojefinance.product.domain.entity.Budget;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class BudgetRepositoryImpl implements BudgetRepository {
    private final BudgetJpaRepository budgetJpaRepository;
    private final BudgetDataMapper budgetDataMapper;

    @Override
    @CacheEvict(value = "budgets", key = "#principalName + '-budgets'")
    public void saveBudget(BudgetEntity budgetEntity, String principalName) {
        budgetJpaRepository.save(budgetEntity);
    }

    @Override
    @Cacheable(value = "budgets", key = "#principalName + '-budgets'")
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
    @Caching(evict = {
            @CacheEvict(value = "budgets", key = "#principalName + '-budgets'"),
            @CacheEvict(value = "budget", key = "#budgetId + '-budget'")
    })
    public void deleteBudget(Long budgetId, String principalName) {
        budgetJpaRepository.deleteById(budgetId);
    }

    @Override
    @Cacheable(value = "budget", key = "#budgetId + '-budget'")
    public Budget getBudgetById(Long budgetId) {
        return budgetJpaRepository.findById(budgetId)
                .map(budgetDataMapper::toBudget)
                .orElse(null);
    }
}
