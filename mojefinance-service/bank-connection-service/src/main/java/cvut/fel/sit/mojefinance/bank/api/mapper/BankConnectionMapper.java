package cvut.fel.sit.mojefinance.bank.api.mapper;

import cvut.fel.sit.mojefinance.bank.domain.dto.ConnectedBanksDomainResponse;
import cvut.fel.sit.mojefinance.bank.domain.entity.BankConnection;
import cvut.fel.sit.mojefinance.openapi.model.Bank;
import cvut.fel.sit.mojefinance.openapi.model.ConnectedBanksResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BankConnectionMapper {
    ConnectedBanksResponse toConnectedBanksResponse(ConnectedBanksDomainResponse domainResponse);

    BankConnection toBankConnectionDomainEntity(Bank apiBank);

    Bank toBankApiEntity(BankConnection domainBankConnection);
}
