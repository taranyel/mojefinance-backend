package cvut.fel.sit.mojefinance.bank.api.mapper;

import cvut.fel.sit.mojefinance.bank.domain.dto.ConnectedBanksDomainResponse;
import cvut.fel.sit.mojefinance.openapi.model.ConnectedBanksResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BankApiMapper {
    ConnectedBanksResponse toConnectedBanksResponse(ConnectedBanksDomainResponse domainResponse);
}
