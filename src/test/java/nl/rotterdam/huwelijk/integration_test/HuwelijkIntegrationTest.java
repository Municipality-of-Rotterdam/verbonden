package nl.rotterdam.huwelijk.integration_test;


import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureEmbeddedDatabase

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface HuwelijkIntegrationTest {
}
