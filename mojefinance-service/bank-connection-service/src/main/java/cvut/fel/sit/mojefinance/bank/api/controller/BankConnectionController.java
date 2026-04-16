package cvut.fel.sit.mojefinance.bank.api.controller;

import cvut.fel.sit.mojefinance.bank.api.mapper.BankConnectionMapper;
import cvut.fel.sit.mojefinance.bank.domain.dto.ConnectBankDomainRequest;
import cvut.fel.sit.mojefinance.bank.domain.dto.ConnectedBanksResponse;
import cvut.fel.sit.mojefinance.bank.domain.service.BankConnectionService;
import cvut.fel.sit.mojefinance.openapi.api.BanksApi;
import cvut.fel.sit.mojefinance.openapi.model.ConnectBankRequest;
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
    public ResponseEntity<Void> connectBank(String authorization, ConnectBankRequest connectBankRequest, String code) {
        ConnectBankDomainRequest domainRequest = bankConnectionMapper.toConnectBankDomainRequest(connectBankRequest, code);
        bankConnectionService.connectBank(domainRequest);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Void> disconnectBank(String authorization, String clientRegistrationId) {
        bankConnectionService.disconnectBank(clientRegistrationId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<cvut.fel.sit.mojefinance.openapi.model.ConnectedBanksResponse> getConnectedBanks(String authorization) {
        ConnectedBanksResponse domainResponse = bankConnectionService.getConnectedBanks();
        cvut.fel.sit.mojefinance.openapi.model.ConnectedBanksResponse apiResponse = bankConnectionMapper.toConnectedBanksResponse(domainResponse);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }
}
