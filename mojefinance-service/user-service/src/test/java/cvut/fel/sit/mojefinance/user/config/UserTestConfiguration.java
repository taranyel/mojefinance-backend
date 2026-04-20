package cvut.fel.sit.mojefinance.user.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = "cvut.fel.sit.mojefinance.user")
public class UserTestConfiguration {
}
