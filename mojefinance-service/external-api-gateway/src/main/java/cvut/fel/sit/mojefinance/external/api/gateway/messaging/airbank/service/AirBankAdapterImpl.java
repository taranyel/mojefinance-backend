package cvut.fel.sit.mojefinance.external.api.gateway.messaging.airbank.service;

import cvut.fel.sit.cs.openapi.model.AuthCodeResponse;
import cvut.fel.sit.mojefinance.external.api.gateway.messaging.airbank.client.AirBankApiFeignClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import static cvut.fel.sit.mojefinance.external.api.gateway.messaging.util.FormDataProvider.createFormDataForTokenRequest;


@Service
@RequiredArgsConstructor
public class AirBankAdapterImpl implements AirBankAdapter {
    private final AirBankApiFeignClient airBankApiFeignClient;

    @Value("${external.oauth2.air-bank.client-id}")
    private String clientId;

    @Value("${external.oauth2.air-bank.client-secret}")
    private String clientSecret;

    @Override
    public void connectAirBank(String code) {
        MultiValueMap<String, String> formData = createFormDataForTokenRequest(code, clientId, clientSecret);
        ResponseEntity<AuthCodeResponse> responseEntity = airBankApiFeignClient.getToken(formData);
        System.out.println("Access token: " + responseEntity.toString());
    }
}
