package cvut.fel.sit.mojefinance.bank.connection.messaging.kb.config;

import cvut.fel.sit.mojefinance.bank.connection.messaging.config.BankFeignConfiguration;
import feign.Client;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KBFeignConfig {
    @Bean
    public Client feignClient(BankFeignConfiguration factory) {
        return factory.createFeignClient("kb-mtls");
    }
}