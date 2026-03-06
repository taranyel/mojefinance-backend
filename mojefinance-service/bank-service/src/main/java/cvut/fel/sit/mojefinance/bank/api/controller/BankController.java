package cvut.fel.sit.mojefinance.bank.api.controller;

import cvut.fel.sit.mojefinance.bank.api.mapper.BankApiMapper;
import cvut.fel.sit.mojefinance.bank.domain.dto.ConnectedBanksDomainResponse;
import cvut.fel.sit.mojefinance.bank.domain.service.BankService;
import cvut.fel.sit.mojefinance.openapi.api.BanksApi;
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
    public ResponseEntity<Void> connectCeskaSporitelna(String authorization, String code) {
        System.out.println("Received request to connect Ceska Sporitelna with code: " + code);
        bankService.connectCeskaSporitelna(code);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> connectCSOB(String authorization, String code) {
        System.out.println("Received request to connect CSOB with code: " + code);
        bankService.connectCSOB(code);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> connectAirBank(String authorization, String code) {
        System.out.println("Received request to connect Air Bank with code: " + code);
        bankService.connectAirBank(code);
        return new ResponseEntity<>(HttpStatus.OK);    }

    @Override
    public ResponseEntity<Void> connectKB(String authorization, String code) {
        System.out.println("Received request to connect KB with code: " + code);
        bankService.connectKB(code);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ConnectedBanksResponse> getConnectedBanks(String authorization) {
        ConnectedBanksDomainResponse domainResponse = bankService.getConnectedBanks(authorization);
        ConnectedBanksResponse apiResponse = bankApiMapper.toConnectedBanksResponse(domainResponse);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }
}
