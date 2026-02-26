package cvut.fel.sit.mojefinance.bank.connection.messaging.csob.service;

import cvut.fel.sit.cs.openapi.model.AuthCodeResponse;
import cvut.fel.sit.mojefinance.bank.connection.messaging.csob.client.CSOBApiFeignClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import static cvut.fel.sit.mojefinance.bank.connection.messaging.util.FormDataProvider.createFormDataForTokenRequest;

@Service
@RequiredArgsConstructor
public class CSOBAdapterImpl implements CSOBAdapter {
    private final CSOBApiFeignClient csobApiFeignClient;

    @Value("${external.oauth2.csob.client-id}")
    private String clientId;

    @Value("${external.oauth2.csob.client-secret}")
    private String clientSecret;

    @Override
    public void connectCSOB(String code) {
        MultiValueMap<String, String> formData = createFormDataForTokenRequest(code, clientId, clientSecret);
        ResponseEntity<AuthCodeResponse> responseEntity = csobApiFeignClient.getToken(formData);
        System.out.println("Access token: " + responseEntity.toString());
    }
}
