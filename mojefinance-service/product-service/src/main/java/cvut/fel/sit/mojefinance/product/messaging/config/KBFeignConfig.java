package cvut.fel.sit.mojefinance.product.messaging.config;

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
