package cvut.fel.sit.mojefinance.bank.data.mapper;

import cvut.fel.sit.mojefinance.bank.data.entity.BankConnectionEntity;
import cvut.fel.sit.mojefinance.bank.domain.entity.BankConnection;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BankConnectionDataMapper {
    @Mapping(target = "clientRegistrationId", source = "id.clientRegistrationId")
    BankConnection toBankConnection(BankConnectionEntity bankConnectionEntity);
}
