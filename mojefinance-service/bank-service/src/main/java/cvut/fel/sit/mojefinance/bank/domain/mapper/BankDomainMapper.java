package cvut.fel.sit.mojefinance.bank.domain.mapper;

import cvut.fel.sit.mojefinance.authorization.data.dto.ConnectAuthorizedClientRequest;
import cvut.fel.sit.mojefinance.bank.data.dto.GetConnectedBanksDataResponse;
import cvut.fel.sit.mojefinance.bank.data.entity.BankEntity;
import cvut.fel.sit.mojefinance.bank.domain.dto.ConnectBankDomainRequest;
import cvut.fel.sit.mojefinance.bank.domain.dto.ConnectedBanksDomainResponse;
import cvut.fel.sit.mojefinance.bank.domain.entity.BankDomainEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BankDomainMapper {
    ConnectedBanksDomainResponse toConnectedBanksDomainResponse(GetConnectedBanksDataResponse dataResponse);

    @Mapping(target = "clientRegistrationId", source = "bankDomainEntity.clientRegistrationId")
    ConnectAuthorizedClientRequest toConnectAuthorizedClientRequest(ConnectBankDomainRequest domainRequest);

    @Mapping(target = "id.clientRegistrationId", source = "clientRegistrationId")
    BankEntity toBankEntity(BankDomainEntity bankDomainEntity);

    @Mapping(target = "clientRegistrationId", source = "id.clientRegistrationId")
    BankDomainEntity toBankDomainEntity(BankEntity bankEntity);
}
