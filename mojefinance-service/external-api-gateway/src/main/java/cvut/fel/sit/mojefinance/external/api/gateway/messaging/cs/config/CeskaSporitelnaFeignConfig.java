package cvut.fel.sit.mojefinance.external.api.gateway.messaging.cs.config;

import cvut.fel.sit.mojefinance.external.api.gateway.messaging.config.BankFeignConfiguration;
import feign.Client;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CeskaSporitelnaFeignConfig {
    @Bean
    public Client ceskaSporitelnafeignClient(BankFeignConfiguration factory) {
        return factory.createFeignClient("ceska-sporitelna-mtls");
    }
}