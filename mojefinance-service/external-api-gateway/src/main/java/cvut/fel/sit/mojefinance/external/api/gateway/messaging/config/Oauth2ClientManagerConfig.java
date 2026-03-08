package cvut.fel.sit.mojefinance.external.api.gateway.messaging.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.JdbcOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.DefaultRefreshTokenTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.client.web.AuthenticatedPrincipalOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

@Configuration
public class Oauth2ClientManagerConfig {

    @Bean
    public RestTemplate oauth2RestTemplate(RestTemplateBuilder builder) {
        RestTemplate restTemplate = builder.build();
        restTemplate.setMessageConverters(Arrays.asList(
                new FormHttpMessageConverter(),
                new OAuth2AccessTokenResponseHttpMessageConverter()
        ));
        restTemplate.setErrorHandler(new OAuth2ErrorResponseErrorHandler());
        return restTemplate;
    }

    @Bean
    public DefaultAuthorizationCodeTokenResponseClient authorizationCodeTokenResponseClient(
            RestTemplate oauth2RestTemplate) {
        DefaultAuthorizationCodeTokenResponseClient client = new DefaultAuthorizationCodeTokenResponseClient();
        client.setRestOperations(oauth2RestTemplate);
        return client;
    }

    @Bean
    public DefaultRefreshTokenTokenResponseClient refreshTokenTokenResponseClient(
            RestTemplate oauth2RestTemplate) {
        DefaultRefreshTokenTokenResponseClient client = new DefaultRefreshTokenTokenResponseClient();
        client.setRestOperations(oauth2RestTemplate);
        return client;
    }

    @Bean
    public OAuth2AuthorizedClientRepository authorizedClientRepository(
            OAuth2AuthorizedClientService authorizedClientService) {
        return new AuthenticatedPrincipalOAuth2AuthorizedClientRepository(authorizedClientService);
    }

    @Bean
    public OAuth2AuthorizedClientService authorizedClientService(
            JdbcTemplate jdbcTemplate,
            ClientRegistrationRepository clientRegistrationRepository) {
        return new JdbcOAuth2AuthorizedClientService(jdbcTemplate, clientRegistrationRepository);
    }

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientRepository authorizedClientRepository,
            DefaultRefreshTokenTokenResponseClient refreshTokenTokenResponseClient) {

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
}