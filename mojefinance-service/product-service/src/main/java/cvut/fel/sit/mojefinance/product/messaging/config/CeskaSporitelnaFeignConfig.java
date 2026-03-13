package cvut.fel.sit.mojefinance.product.messaging.config;

import feign.Client;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CeskaSporitelnaFeignConfig {
    @Bean
    public Client ceskaSporitelnaFeignClient(FeignConfiguration factory) {
        return factory.createFeignClient("ceska-sporitelna-mtls");
    }
}
