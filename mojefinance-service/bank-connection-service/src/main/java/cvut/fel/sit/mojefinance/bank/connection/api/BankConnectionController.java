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
}
