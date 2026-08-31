package nl.rotterdam.verbonden.app_local;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;

import static io.zonky.test.db.AutoConfigureEmbeddedDatabase.DatabaseProvider.ZONKY;

// Inlined rather than reusing core's @VerbondenIntegrationTest: that annotation now lives in
// core's test sources (src/test/java), which are private to the core module and not visible
// on app-local's test classpath.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureEmbeddedDatabase(provider = ZONKY)
@AutoConfigureTestRestTemplate
class VerbondenApplicationTests {

    @Test
    void contextLoads() {
    }
}
