package cvut.fel.sit.mojefinance.bank.domain.mapper;

import cvut.fel.sit.mojefinance.bank.domain.dto.ConnectedBanksDomainResponse;
import cvut.fel.sit.mojefinance.external.api.gateway.data.dto.ConnectedBanksResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BankDomainMapper {
    ConnectedBanksDomainResponse toConnectedBanksDomainResponse(ConnectedBanksResponse dataResponse);
}
