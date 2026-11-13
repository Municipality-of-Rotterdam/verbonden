package nl.rotterdam.verbonden.app_local;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static io.zonky.test.db.AutoConfigureEmbeddedDatabase.DatabaseProvider.EMBEDDED;
import static org.assertj.core.api.Assertions.assertThat;

// Inlined rather than reusing core's @VerbondenIntegrationTest: that annotation now lives in
// core's test sources (src/test/java), which are private to the core module and not visible
// on app-local's test classpath.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureEmbeddedDatabase(provider = EMBEDDED)
class VerbondenApplicationTests {

    @LocalServerPort
    private int port;

    @Test
    void contextLoads() {
    }

    @Test
    void sessionCookieIsSecureAndHttpOnly() throws Exception {
        HttpRequest request = HttpRequest.newBuilder(
                        URI.create("http://localhost:" + port + "/mijn-dag/" + java.util.UUID.randomUUID()))
                .build();
        HttpResponse<Void> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.discarding());

        assertThat(response.headers().allValues("Set-Cookie"))
                .anySatisfy(cookie -> assertThat(cookie)
                        .startsWith("JSESSIONID=")
                        .containsIgnoringCase("; Secure")
                        .containsIgnoringCase("; HttpOnly"));
    }
}
