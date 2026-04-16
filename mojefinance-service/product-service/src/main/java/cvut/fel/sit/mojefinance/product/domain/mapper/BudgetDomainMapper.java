package cvut.fel.sit.mojefinance.product.domain.mapper;

import cvut.fel.sit.mojefinance.product.data.entity.BudgetEntity;
import cvut.fel.sit.mojefinance.product.domain.entity.Budget;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BudgetDomainMapper {

    @Mapping(target = "amount", source = "amount.value")
    @Mapping(target = "currency", source = "amount.currency")
    BudgetEntity toBudgetEntity(Budget budget);
}
