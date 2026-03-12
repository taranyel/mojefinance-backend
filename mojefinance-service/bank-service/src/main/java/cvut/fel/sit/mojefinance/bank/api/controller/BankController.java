package cvut.fel.sit.mojefinance.bank.api.controller;

import cvut.fel.sit.mojefinance.bank.api.mapper.BankApiMapper;
import cvut.fel.sit.mojefinance.bank.domain.dto.ConnectBankDomainRequest;
import cvut.fel.sit.mojefinance.bank.domain.dto.GetConnectedBanksDomainResponse;
import cvut.fel.sit.mojefinance.bank.domain.entity.BankDomainEntity;
import cvut.fel.sit.mojefinance.bank.domain.service.BankService;
import cvut.fel.sit.mojefinance.openapi.api.BanksApi;
import cvut.fel.sit.mojefinance.openapi.model.Bank;
import cvut.fel.sit.mojefinance.openapi.model.ConnectedBanksResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BankController implements BanksApi {
    private final BankService bankService;
    private final BankApiMapper bankApiMapper;

    @Override
    public ResponseEntity<Bank> connectBank(String authorization, Bank bank, String code) {
        ConnectBankDomainRequest domainRequest = ConnectBankDomainRequest.builder()
                .code(code)
                .bankDomainEntity(bankApiMapper.toBankDomainEntity(bank))
                .build();
        BankDomainEntity bankDomainEntity = bankService.connectBank(domainRequest);
        Bank apiBank = bankApiMapper.toBankApiEntity(bankDomainEntity);
        return new ResponseEntity<>(apiBank, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Void> disconnectBank(String authorization, String clientRegistrationId) {
        bankService.disconnectBank(clientRegistrationId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ConnectedBanksResponse> getConnectedBanks(String authorization) {
        GetConnectedBanksDomainResponse domainResponse = bankService.getConnectedBanks();
        ConnectedBanksResponse apiResponse = bankApiMapper.toConnectedBanksResponse(domainResponse);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }
}
