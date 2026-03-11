package cvut.fel.sit.mojefinance.external.api.gateway.messaging.reif.config;

import cvut.fel.sit.mojefinance.external.api.gateway.messaging.config.FeignConfiguration;
import feign.Client;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReiffeisenBankFeignConfig {
    @Bean
    public Client reifFeignClient(FeignConfiguration factory) {
        return factory.createFeignClient("reiffeisen-bank-mtls");
    }
}