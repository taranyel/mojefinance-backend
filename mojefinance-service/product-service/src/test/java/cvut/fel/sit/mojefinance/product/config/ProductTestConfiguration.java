package cvut.fel.sit.mojefinance.product.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = "cvut.fel.sit.mojefinance.product",
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = "cvut\\.fel\\.sit\\.mojefinance\\.product\\.messaging\\.config\\..*"
        ))
public class ProductTestConfiguration {
}
