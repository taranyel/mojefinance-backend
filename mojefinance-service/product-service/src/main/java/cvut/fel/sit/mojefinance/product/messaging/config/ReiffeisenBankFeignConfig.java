package cvut.fel.sit.mojefinance.product.messaging.config;

import feign.Client;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReiffeisenBankFeignConfig {
    @Bean
    public Client reiffeisenBankFeignClient(FeignConfiguration factory) {
        return factory.createFeignClient("reiffeisen-bank-mtls");
    }
}
