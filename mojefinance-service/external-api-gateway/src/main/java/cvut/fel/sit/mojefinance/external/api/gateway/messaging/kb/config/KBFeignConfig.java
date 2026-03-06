package cvut.fel.sit.mojefinance.external.api.gateway.messaging.kb.config;

import cvut.fel.sit.mojefinance.external.api.gateway.messaging.config.BankFeignConfiguration;
import feign.Client;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KBFeignConfig {
    @Bean
    public Client kbFeignClient(BankFeignConfiguration factory) {
        return factory.createFeignClient("kb-mtls");
    }
}