package cvut.fel.sit.mojefinance.bank.connection.api;

import cvut.fel.sit.mojefinance.bank.connection.domain.service.BankConnectionService;
import cvut.fel.sit.mojefinance.openapi.api.BankConnectionApi;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BankConnectionController implements BankConnectionApi {
    private final BankConnectionService bankConnectionService;

    @Override
    public ResponseEntity<Void> connectCeskaSporitelna(String authorization, String code) {
        System.out.println("Received request to connect Ceska Sporitelna with code: " + code);
        bankConnectionService.connectCeskaSporitelna(code);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> connectCSOB(String authorization, String code) {
        System.out.println("Received request to connect CSOB with code: " + code);
        bankConnectionService.connectCSOB(code);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> connectAirBank(String authorization, String code) {
        System.out.println("Received request to connect Air Bank with code: " + code);
        bankConnectionService.connectAirBank(code);
        return new ResponseEntity<>(HttpStatus.OK);    }

    @Override
    public ResponseEntity<Void> connectKB(String authorization, String code) {
        System.out.println("Received request to connect KB with code: " + code);
        bankConnectionService.connectKB(code);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> connectReiffeisenBank(String authorization, String code) {
        System.out.println("Received request to connect Reiffeisen Bank with code: " + code);
        bankConnectionService.connectReiffeisenBank(code);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
