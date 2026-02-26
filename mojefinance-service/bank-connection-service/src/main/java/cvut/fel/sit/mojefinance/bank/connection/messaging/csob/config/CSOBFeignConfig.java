package cvut.fel.sit.mojefinance.bank.connection.messaging.csob.config;

import cvut.fel.sit.mojefinance.bank.connection.messaging.config.BankFeignConfiguration;
import feign.Client;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CSOBFeignConfig {
    @Bean
    public Client feignClient(BankFeignConfiguration factory) {
        return factory.createFeignClient("csob-mtls");
    }
}