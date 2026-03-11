package cvut.fel.sit.mojefinance.external.api.gateway.messaging.kb.config;

import cvut.fel.sit.mojefinance.external.api.gateway.messaging.config.FeignConfiguration;
import feign.Client;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KBFeignConfig {
    @Bean
    public Client kbFeignClient(FeignConfiguration factory) {
        return factory.createFeignClient("kb-mtls");
    }
}