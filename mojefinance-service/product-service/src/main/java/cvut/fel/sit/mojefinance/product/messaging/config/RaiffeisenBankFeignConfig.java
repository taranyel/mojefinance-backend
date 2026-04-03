package cvut.fel.sit.mojefinance.product.messaging.config;

import feign.Client;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RaiffeisenBankFeignConfig {
    @Bean
    public Client raiffeisenBankFeignClient(FeignConfiguration factory) {
        return factory.createFeignClient("raiffeisen-bank-mtls");
    }
}
