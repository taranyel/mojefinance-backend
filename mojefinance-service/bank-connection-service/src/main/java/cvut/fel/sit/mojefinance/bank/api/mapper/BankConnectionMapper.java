package cvut.fel.sit.mojefinance.bank.api.mapper;

import cvut.fel.sit.mojefinance.bank.domain.dto.ConnectBankDomainRequest;
import cvut.fel.sit.mojefinance.bank.domain.dto.ConnectedBanksResponse;
import cvut.fel.sit.mojefinance.openapi.model.ConnectBankRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BankConnectionMapper {
    ConnectBankDomainRequest toConnectBankDomainRequest(ConnectBankRequest connectBankRequest, String code);

    cvut.fel.sit.mojefinance.openapi.model.ConnectedBanksResponse toConnectedBanksResponse(ConnectedBanksResponse domainResponse);
}
