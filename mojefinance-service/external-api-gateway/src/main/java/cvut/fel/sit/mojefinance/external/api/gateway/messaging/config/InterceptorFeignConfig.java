package cvut.fel.sit.mojefinance.external.api.gateway.messaging.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;

@Configuration
public class InterceptorFeignConfig {
    @Bean
    public RequestInterceptor oauth2Interceptor(OAuth2AuthorizedClientManager apiAManager) {
        return new BankFeignInterceptor(apiAManager);
    }
}
