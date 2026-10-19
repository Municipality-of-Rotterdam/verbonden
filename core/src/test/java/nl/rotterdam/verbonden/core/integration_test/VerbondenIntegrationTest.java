package nl.rotterdam.verbonden.core.integration_test;


import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

import static io.zonky.test.db.AutoConfigureEmbeddedDatabase.DatabaseProvider.EMBEDDED;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "verbonden.logout.beheer-url=/logout",
        "verbonden.logout.burger-url=/uitloggen"
})
@AutoConfigureEmbeddedDatabase(provider = EMBEDDED)
@AutoConfigureTestRestTemplate
@Import(InstelbareKlok.Config.class)

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface VerbondenIntegrationTest {
}
