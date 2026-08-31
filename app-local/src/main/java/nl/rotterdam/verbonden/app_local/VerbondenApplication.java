package nl.rotterdam.verbonden.app_local;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

// core's and remote-local's classes/entities/repositories live under nl.rotterdam.verbonden.core
// / .remote_local, which are sibling packages of nl.rotterdam.verbonden.app_local rather than a
// parent. scanBasePackages widens component-scanning to find them, but @EnableJpaRepositories/
// @EntityScan need their own explicit basePackages: Spring Boot's JPA autoconfiguration resolves
// their default base package from the @SpringBootApplication class's own package, not from
// scanBasePackages, so without these two annotations Spring Data repository proxies (e.g.
// BabsRepository) are never created.
@SpringBootApplication(scanBasePackages = "nl.rotterdam.verbonden")
@EnableJpaRepositories(basePackages = "nl.rotterdam.verbonden")
@EntityScan(basePackages = "nl.rotterdam.verbonden")
public class VerbondenApplication {

    static void main(String[] args) {
        SpringApplication.run(VerbondenApplication.class, args);
    }
}
