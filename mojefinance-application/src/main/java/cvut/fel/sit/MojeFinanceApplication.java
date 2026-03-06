package cvut.fel.sit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = {
        "cvut.fel.sit.mojefinance.external.api.gateway.messaging.airbank.client",
        "cvut.fel.sit.mojefinance.external.api.gateway.messaging.cs.client",
        "cvut.fel.sit.mojefinance.external.api.gateway.messaging.csob.client",
        "cvut.fel.sit.mojefinance.external.api.gateway.messaging.kb.client",
        "cvut.fel.sit.mojefinance.external.api.gateway.messaging.reif.client"
})
public class MojeFinanceApplication {
    public static void main(String[] args) {
        SpringApplication.run(MojeFinanceApplication.class, args);
    }
}
