package cvut.fel.sit.mojefinance.bank.domain.mapper;

import cvut.fel.sit.mojefinance.authorization.data.dto.ConnectAuthorizedClientRequest;
import cvut.fel.sit.mojefinance.bank.data.dto.ConnectedBanksDataResponse;
import cvut.fel.sit.mojefinance.bank.data.entity.BankConnectionEntity;
import cvut.fel.sit.mojefinance.bank.domain.dto.ConnectBankDomainRequest;
import cvut.fel.sit.mojefinance.bank.domain.dto.ConnectedBanksDomainResponse;
import cvut.fel.sit.mojefinance.bank.domain.entity.BankConnection;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BankConnectionDomainMapper {
    ConnectedBanksDomainResponse toConnectedBanksDomainResponse(ConnectedBanksDataResponse dataResponse);

    @Mapping(target = "clientRegistrationId", source = "bankConnection.clientRegistrationId")
    ConnectAuthorizedClientRequest toConnectAuthorizedClientRequest(ConnectBankDomainRequest domainRequest);

    @Mapping(target = "id.clientRegistrationId", source = "clientRegistrationId")
    BankConnectionEntity toBankConnectionEntity(BankConnection bankConnection);

    @Mapping(target = "clientRegistrationId", source = "id.clientRegistrationId")
    BankConnection toBankConnectionDomainEntity(BankConnectionEntity bankConnectionEntity);
}
