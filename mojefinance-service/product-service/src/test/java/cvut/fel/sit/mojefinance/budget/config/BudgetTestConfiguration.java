package cvut.fel.sit.mojefinance.budget.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = "cvut.fel.sit.mojefinance.budget")
@EntityScan(basePackages = {
        "cvut.fel.sit.mojefinance.budget.data.entity"
})
@EnableJpaRepositories(basePackages = {
        "cvut.fel.sit.mojefinance.budget.data.repository"
})
public class BudgetTestConfiguration {
}
