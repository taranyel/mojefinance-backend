package cvut.fel.sit.mojefinance.external.api.gateway.messaging.cs.config;

import cvut.fel.sit.mojefinance.external.api.gateway.messaging.config.FeignConfiguration;
import cvut.fel.sit.mojefinance.external.api.gateway.util.Constants;
import feign.Client;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CeskaSporitelnaFeignConfig {
    @Bean
    public Client ceskaSporitelnafeignClient(FeignConfiguration factory) {
        return factory.createFeignClient(Constants.CESKA_SPORITELNA_SSL_BUNDLE_NAME);
    }
}