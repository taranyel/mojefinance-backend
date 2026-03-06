package cvut.fel.sit.mojefinance.bank.domain.mapper;

import cvut.fel.sit.mojefinance.bank.domain.dto.ConnectedBanksDataResponse;
import cvut.fel.sit.mojefinance.bank.domain.dto.ConnectedBanksDomainResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BankDomainMapper {
    ConnectedBanksDomainResponse toConnectedBanksDomainResponse(ConnectedBanksDataResponse dataResponse);
}
