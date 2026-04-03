package cvut.fel.sit.mojefinance.bank.api.controller;

import cvut.fel.sit.mojefinance.bank.api.mapper.BankConnectionMapper;
import cvut.fel.sit.mojefinance.bank.domain.dto.ConnectBankDomainRequest;
import cvut.fel.sit.mojefinance.bank.domain.dto.ConnectedBanksDomainResponse;
import cvut.fel.sit.mojefinance.bank.domain.entity.BankConnection;
import cvut.fel.sit.mojefinance.bank.domain.service.BankConnectionService;
import cvut.fel.sit.mojefinance.openapi.api.BanksApi;
import cvut.fel.sit.mojefinance.openapi.model.ConnectedBanksResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BankConnectionController implements BanksApi {
    private final BankConnectionService bankConnectionService;
    private final BankConnectionMapper bankConnectionMapper;

    @Override
    public ResponseEntity<cvut.fel.sit.mojefinance.openapi.model.Bank> connectBank(String authorization, cvut.fel.sit.mojefinance.openapi.model.Bank bank, String code) {
        ConnectBankDomainRequest domainRequest = ConnectBankDomainRequest.builder()
                .code(code)
                .bankConnection(bankConnectionMapper.toBankConnectionDomainEntity(bank))
                .build();
        BankConnection bankConnectionDomainEntity = bankConnectionService.connectBank(domainRequest);
        cvut.fel.sit.mojefinance.openapi.model.Bank apiBank = bankConnectionMapper.toBankApiEntity(bankConnectionDomainEntity);
        return new ResponseEntity<>(apiBank, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Void> disconnectBank(String authorization, String clientRegistrationId) {
        bankConnectionService.disconnectBank(clientRegistrationId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ConnectedBanksResponse> getConnectedBanks(String authorization) {
        ConnectedBanksDomainResponse domainResponse = bankConnectionService.getConnectedBanks();
        ConnectedBanksResponse apiResponse = bankConnectionMapper.toConnectedBanksResponse(domainResponse);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }
}
