package cvut.fel.sit.mojefinance.external.api.gateway.messaging.cs.service;

import cvut.fel.sit.cs.openapi.model.AuthCodeResponse;
import cvut.fel.sit.mojefinance.external.api.gateway.messaging.cs.client.CeskaSporitelnaApiFeignClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import static cvut.fel.sit.mojefinance.external.api.gateway.messaging.util.FormDataProvider.createFormDataForTokenRequest;


@Service
@RequiredArgsConstructor
public class CeskaSporitelnaAdapterImpl implements CeskaSporitelnaAdapter {
    private final CeskaSporitelnaApiFeignClient ceskaSporitelnaApiFeignClient;

    @Value("${external.oauth2.ceska-sporitelna.client-id}")
    private String clientId;

    @Value("${external.oauth2.ceska-sporitelna.client-secret}")
    private String clientSecret;

    @Override
    public void connectCeskaSporitelna(String code) {
        MultiValueMap<String, String> formData = createFormDataForTokenRequest(code, clientId, clientSecret);
        ResponseEntity<AuthCodeResponse> responseEntity = ceskaSporitelnaApiFeignClient.getToken(formData);
        System.out.println("Access token: " + responseEntity.toString());
    }
}
