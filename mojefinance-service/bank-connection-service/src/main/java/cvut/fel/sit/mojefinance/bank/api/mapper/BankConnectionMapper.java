package cvut.fel.sit.mojefinance.bank.api.mapper;

import cvut.fel.sit.mojefinance.bank.domain.dto.ConnectBankDomainRequest;
import cvut.fel.sit.mojefinance.bank.domain.dto.ConnectedBanksResponse;
import cvut.fel.sit.mojefinance.openapi.model.ConnectBankRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BankConnectionMapper {

    @Mapping(target = "code", source = "code")
    @Mapping(target = "bankConnection", source = "connectBankRequest.bankConnection")
    ConnectBankDomainRequest toConnectBankDomainRequest(ConnectBankRequest connectBankRequest, String code);

    cvut.fel.sit.mojefinance.openapi.model.ConnectedBanksResponse toConnectedBanksResponse(ConnectedBanksResponse domainResponse);
}
