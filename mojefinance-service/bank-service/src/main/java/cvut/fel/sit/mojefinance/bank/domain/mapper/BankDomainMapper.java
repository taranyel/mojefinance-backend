package cvut.fel.sit.mojefinance.bank.domain.mapper;

import cvut.fel.sit.mojefinance.bank.data.dto.ConnectedBanksDataResponse;
import cvut.fel.sit.mojefinance.bank.data.entity.BankEntity;
import cvut.fel.sit.mojefinance.bank.domain.dto.ConnectedBanksDomainResponse;
import cvut.fel.sit.mojefinance.bank.domain.entity.Bank;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BankDomainMapper {
    ConnectedBanksDomainResponse toConnectedBanksDomainResponse(ConnectedBanksDataResponse dataResponse);

    @Mapping(target = "name", source = "bankName")
    @Mapping(target = "connectionStatus", source = "bankConnectionStatus")
    Bank toBank(BankEntity bankEntity);
}
