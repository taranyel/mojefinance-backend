package cvut.fel.sit.mojefinance.authorization.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;

public class AuthorizedClientService implements OAuth2AuthorizedClientService {

    private final OAuth2AuthorizedClientService delegate;
    private final TextEncryptor encryptor;

    public AuthorizedClientService(OAuth2AuthorizedClientService delegate, String password, String salt) {
        this.delegate = delegate;
        this.encryptor = Encryptors.text(password, salt);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends OAuth2AuthorizedClient> T loadAuthorizedClient(String clientRegistrationId, String principalName) {
        OAuth2AuthorizedClient authorizedClient = delegate.loadAuthorizedClient(clientRegistrationId, principalName);
        if (authorizedClient == null) {
            return null;
        }
        return (T) decrypt(authorizedClient);
    }

    @Override
    public void saveAuthorizedClient(OAuth2AuthorizedClient authorizedClient, Authentication principal) {
        delegate.saveAuthorizedClient(encrypt(authorizedClient), principal);
    }

    @Override
    public void removeAuthorizedClient(String clientRegistrationId, String principalName) {
        delegate.removeAuthorizedClient(clientRegistrationId, principalName);
    }

    private OAuth2AuthorizedClient encrypt(OAuth2AuthorizedClient client) {
        return copyWithTransformedTokens(client, encryptor::encrypt);
    }

    private OAuth2AuthorizedClient decrypt(OAuth2AuthorizedClient client) {
        return copyWithTransformedTokens(client, encryptor::decrypt);
    }

    private OAuth2AuthorizedClient copyWithTransformedTokens(OAuth2AuthorizedClient client, java.util.function.Function<String, String> transformer) {
        OAuth2AccessToken accessToken = client.getAccessToken();
        OAuth2AccessToken transformedAccessToken = new OAuth2AccessToken(
                accessToken.getTokenType(),
                transformer.apply(accessToken.getTokenValue()),
                accessToken.getIssuedAt(),
                accessToken.getExpiresAt(),
                accessToken.getScopes());

        OAuth2RefreshToken transformedRefreshToken = null;
        if (client.getRefreshToken() != null) {
            transformedRefreshToken = new OAuth2RefreshToken(
                    transformer.apply(client.getRefreshToken().getTokenValue()),
                    client.getRefreshToken().getIssuedAt());
        }

        return new OAuth2AuthorizedClient(
                client.getClientRegistration(),
                client.getPrincipalName(),
                transformedAccessToken,
                transformedRefreshToken);
    }
}