package cvut.fel.sit.mojefinance.external.api.gateway.messaging.csob.config;

import cvut.fel.sit.mojefinance.external.api.gateway.messaging.config.BankFeignConfiguration;
import feign.Client;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CSOBFeignConfig {
    @Bean
    public Client csobFeignClient(BankFeignConfiguration factory) {
        return factory.createFeignClient("csob-mtls");
    }
}