package cvut.fel.sit.mojefinance.bank.api.mapper;

import cvut.fel.sit.mojefinance.bank.domain.dto.ConnectedBanksDomainResponse;
import cvut.fel.sit.mojefinance.bank.domain.entity.BankDomainEntity;
import cvut.fel.sit.mojefinance.openapi.model.Bank;
import cvut.fel.sit.mojefinance.openapi.model.ConnectedBanksResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BankMapper {
    ConnectedBanksResponse toConnectedBanksResponse(ConnectedBanksDomainResponse domainResponse);

    BankDomainEntity toBankDomainEntity(Bank apiBank);

    Bank toBankApiEntity(BankDomainEntity domainBank);
}
