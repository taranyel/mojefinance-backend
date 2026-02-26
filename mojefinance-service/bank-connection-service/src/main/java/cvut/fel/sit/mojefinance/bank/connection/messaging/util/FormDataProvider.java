package cvut.fel.sit.mojefinance.bank.connection.messaging.util;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@RequiredArgsConstructor
public class FormDataProvider {

    @Value("${external.oauth2.redirect-uri}")
    private static String redirectUri;

    public static MultiValueMap<String, String> createFormDataForTokenRequest(String code, String clientId, String clientSecret) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("code", code);
        formData.add("redirect_uri", redirectUri);
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        return formData;
    }
}
