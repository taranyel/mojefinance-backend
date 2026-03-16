package cvut.fel.sit.mojefinance.authorization.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.client.JdbcOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.DefaultRefreshTokenTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.OAuth2RefreshTokenGrantRequest;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequestEntityConverter;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.client.web.AuthenticatedPrincipalOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static cvut.fel.sit.shared.util.Constants.CSOB_CLIENT_REGISTRATION_ID;
import static cvut.fel.sit.shared.util.Constants.KB_CLIENT_REGISTRATION_ID;
import static cvut.fel.sit.shared.util.Constants.CSOB_API_KEY_HEADER;

@Configuration
@RequiredArgsConstructor
public class Oauth2ClientManagerConfig {
    private final RestTemplateBuilder builder;
    private final SslBundles sslBundles;

    @Value("${external.api.csob.apikey}")
    private String csobApiKey;

    @Bean
    @Qualifier("kbRestTemplate")
    public RestTemplate kbRestTemplate() {
        return baseSslRestTemplate("kb-mtls");
    }

    @Bean
    @Qualifier("csobRestTemplate")
    public RestTemplate csobRestTemplate() {
        return baseSslRestTemplate("csob-mtls");
    }

    @Bean
    @Qualifier("defaultRestTemplate")
    public RestTemplate defaultRestTemplate() {
        RestTemplate restTemplate = builder.build();

        restTemplate.setMessageConverters(getCustomMessageConverters());
        restTemplate.setErrorHandler(new OAuth2ErrorResponseErrorHandler());
        return restTemplate;
    }

    @Bean
    public OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> authorizationCodeTokenResponseClient(
            @Qualifier("kbRestTemplate") RestTemplate kbTemplate,
            @Qualifier("csobRestTemplate") RestTemplate csobTemplate,
            @Qualifier("defaultRestTemplate") RestTemplate defaultTemplate) {

        Map<String, DefaultAuthorizationCodeTokenResponseClient> clients = Map.of(
                KB_CLIENT_REGISTRATION_ID, createAuthClient(kbTemplate),
                CSOB_CLIENT_REGISTRATION_ID, createAuthClient(csobTemplate)
        );

        DefaultAuthorizationCodeTokenResponseClient fallbackClient = createAuthClient(defaultTemplate);

        return request -> {
            String regId = request.getClientRegistration().getRegistrationId();
            return clients.getOrDefault(regId, fallbackClient).getTokenResponse(request);
        };
    }

    @Bean
    public OAuth2AccessTokenResponseClient<OAuth2RefreshTokenGrantRequest> refreshTokenTokenResponseClient(
            @Qualifier("kbRestTemplate") RestTemplate kbTemplate,
            @Qualifier("csobRestTemplate") RestTemplate csobTemplate,
            @Qualifier("defaultRestTemplate") RestTemplate defaultTemplate) {

        Map<String, DefaultRefreshTokenTokenResponseClient> clients = Map.of(
                KB_CLIENT_REGISTRATION_ID, createRefreshClient(kbTemplate),
                CSOB_CLIENT_REGISTRATION_ID, createRefreshClient(csobTemplate)
        );

        DefaultRefreshTokenTokenResponseClient fallbackClient = createRefreshClient(defaultTemplate);

        return request -> {
            String regId = request.getClientRegistration().getRegistrationId();
            return clients.getOrDefault(regId, fallbackClient).getTokenResponse(request);
        };
    }

    @Bean
    public OAuth2AuthorizedClientRepository authorizedClientRepository(
            OAuth2AuthorizedClientService authorizedClientService) {
        return new AuthenticatedPrincipalOAuth2AuthorizedClientRepository(authorizedClientService);
    }

    @Bean
    public OAuth2AuthorizedClientService authorizedClientService(
            JdbcTemplate jdbcTemplate,
            ClientRegistrationRepository clientRegistrationRepository,
            @Value("${security.oauth2.encryption.password}") String password,
            @Value("${security.oauth2.encryption.salt}") String salt) {

        JdbcOAuth2AuthorizedClientService jdbcService =
                new JdbcOAuth2AuthorizedClientService(jdbcTemplate, clientRegistrationRepository);

        return new EncryptingOAuth2AuthorizedClientService(jdbcService, password, salt);
    }

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientRepository authorizedClientRepository,
            OAuth2AccessTokenResponseClient<OAuth2RefreshTokenGrantRequest> refreshTokenTokenResponseClient) {

        OAuth2AuthorizedClientProvider authorizedClientProvider =
                OAuth2AuthorizedClientProviderBuilder.builder()
                        .authorizationCode()
                        .refreshToken(configurer -> configurer.accessTokenResponseClient(refreshTokenTokenResponseClient))
                        .build();

        DefaultOAuth2AuthorizedClientManager manager = new DefaultOAuth2AuthorizedClientManager(
                clientRegistrationRepository,
                authorizedClientRepository);
        manager.setAuthorizedClientProvider(authorizedClientProvider);

        return manager;
    }

    public RestTemplate baseSslRestTemplate(String bundleName) {
        RestTemplate restTemplate = builder
                .setSslBundle(sslBundles.getBundle(bundleName))
                .build();

        restTemplate.setMessageConverters(getCustomMessageConverters());
        restTemplate.setErrorHandler(new OAuth2ErrorResponseErrorHandler());
        return restTemplate;
    }

    private List<HttpMessageConverter<?>> getCustomMessageConverters() {
        OAuth2AccessTokenResponseHttpMessageConverter tokenConverter = new OAuth2AccessTokenResponseHttpMessageConverter();

        tokenConverter.setSupportedMediaTypes(Arrays.asList(
                MediaType.APPLICATION_JSON,
                new MediaType("application", "*+json"),
                new MediaType("text", "json")
        ));

        return Arrays.asList(new FormHttpMessageConverter(), tokenConverter);
    }

    private DefaultRefreshTokenTokenResponseClient createRefreshClient(RestTemplate template) {
        DefaultRefreshTokenTokenResponseClient client = new DefaultRefreshTokenTokenResponseClient();
        client.setRestOperations(template);
        return client;
    }

    private DefaultAuthorizationCodeTokenResponseClient createAuthClient(RestTemplate template) {
        DefaultAuthorizationCodeTokenResponseClient client = new DefaultAuthorizationCodeTokenResponseClient();
        client.setRestOperations(template);

        OAuth2AuthorizationCodeGrantRequestEntityConverter defaultConverter = new OAuth2AuthorizationCodeGrantRequestEntityConverter();

        client.setRequestEntityConverter(request -> {
            RequestEntity<?> entity = defaultConverter.convert(request);
            HttpHeaders headers = new HttpHeaders();
            assert entity != null;
            headers.putAll(entity.getHeaders());

            String registrationId = request.getClientRegistration().getRegistrationId();
            if (CSOB_CLIENT_REGISTRATION_ID.equals(registrationId)) {
                headers.add(CSOB_API_KEY_HEADER, csobApiKey);
            }

            return new RequestEntity<>(entity.getBody(), headers, entity.getMethod(), entity.getUrl());
        });

        return client;
    }
}