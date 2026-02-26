package cvut.fel.sit.mojefinance.bank.connection.messaging.kb.service;

import cvut.fel.sit.cs.openapi.model.AuthCodeResponse;
import cvut.fel.sit.mojefinance.bank.connection.messaging.kb.client.KBApiFeignClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import static cvut.fel.sit.mojefinance.bank.connection.messaging.util.FormDataProvider.createFormDataForTokenRequest;

@Service
@RequiredArgsConstructor
public class KBAdapterImpl implements KBAdapter {
    private final KBApiFeignClient kbApiFeignClient;

    @Value("${external.oauth2.kb.client-id}")
    private String clientId;

    @Value("${external.oauth2.kb.client-secret}")
    private String clientSecret;

    @Override
    public void connectKB(String code) {
        MultiValueMap<String, String> formData = createFormDataForTokenRequest(code, clientId, clientSecret);
        ResponseEntity<AuthCodeResponse> responseEntity = kbApiFeignClient.getToken(formData);
        System.out.println("Access token: " + responseEntity.toString());
    }
}
