package cvut.fel.sit.mojefinance.product.messaging.config;

import feign.Client;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CsobFeignConfig {
    @Bean
    public Client csobFeignClient(FeignConfiguration factory) {
        return factory.createFeignClient("csob-mtls");
    }
}
