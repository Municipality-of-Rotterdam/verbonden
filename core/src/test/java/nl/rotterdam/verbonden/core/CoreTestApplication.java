package nl.rotterdam.verbonden.core;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Test-only bootstrap so {@code @SpringBootTest} can build a context for core's own
 * integration tests. Core is deliberately not startable in production (see module
 * description); this class exists solely under {@code src/test} to anchor component/
 * entity/repository scanning for tests, without making core standalone-startable.
 */
@SpringBootApplication
class CoreTestApplication {
}
