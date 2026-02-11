package cvut.fel.sit.mojefinance.bank.connection.messaging.service;

import cvut.fel.sit.cs.openapi.model.AuthCodeResponse;
import cvut.fel.sit.mojefinance.bank.connection.messaging.client.CeskaSporitelnaApiFeignClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Service
@RequiredArgsConstructor
public class CeskaSporitelnaAdapterImpl implements CeskaSporitelnaAdapter {
    private final CeskaSporitelnaApiFeignClient ceskaSporitelnaApiFeignClient;

    @Value("${external.oauth2.ceska-sporitelna.client-id}")
    private String clientId;

    @Value("${external.oauth2.ceska-sporitelna.client-secret}")
    private String clientSecret;

    @Value("${external.oauth2.ceska-sporitelna.redirect-uri}")
    private String redirectUri;

    @Override
    public void connectCeskaSporitelna(String code) {
        MultiValueMap<String, String> formData = createFormDataForTokenRequest(code);
        ResponseEntity<AuthCodeResponse> responseEntity = ceskaSporitelnaApiFeignClient.getToken(formData);
        System.out.println("Access token: " + responseEntity.toString());
    }

    private MultiValueMap<String, String> createFormDataForTokenRequest(String code) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("code", code);
        formData.add("redirect_uri", redirectUri);
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        return formData;
    }
}
