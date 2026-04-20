package cvut.fel.sit.mojefinance.budget.data.mapper;

import cvut.fel.sit.mojefinance.budget.data.entity.BudgetEntity;
import cvut.fel.sit.mojefinance.budget.domain.entity.Budget;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BudgetDataMapper {

    @Mapping(target = "amount.currency", source = "currency")
    @Mapping(target = "amount.value", source = "amount")
    Budget toBudget(BudgetEntity budgetEntity);
}
