package nl.rotterdam.verbonden.app_local;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.io.IOException;
import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.zonky.test.db.AutoConfigureEmbeddedDatabase.DatabaseProvider.EMBEDDED;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Wie via een link (bijv. de uitnodigingslink van de partner) binnenkomt zonder ingelogd te zijn, moet na het
 * inloggen op die link terugkomen en niet op de startpagina.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureEmbeddedDatabase(provider = EMBEDDED)
class InloggenRedirectTest {

    private static final Pattern INLOGLINK_VAN_PIETER =
            Pattern.compile("999990020.*?href=\"([^\"]+)\"", Pattern.DOTALL);

    @LocalServerPort
    private int port;

    private final HttpClient client = HttpClient.newBuilder()
            .cookieHandler(new CookieManager())
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    @Test
    void naInloggenTerugNaarDeOpgevraagdePagina() throws Exception {
        String uitnodigingslink = "/mijn-dag/" + UUID.randomUUID();

        HttpResponse<String> zonderLogin = get(URI.create(base() + uitnodigingslink));
        assertThat(zonderLogin.statusCode()).isEqualTo(302);
        URI inloggen = volgRedirects(URI.create(base()).resolve(location(zonderLogin)));
        assertThat(inloggen.getPath()).isEqualTo("/inloggen");

        HttpResponse<String> inlogpagina = get(inloggen);
        Matcher link = INLOGLINK_VAN_PIETER.matcher(inlogpagina.body());
        assertThat(link.find()).as("inloglink voor 999990020").isTrue();

        HttpResponse<String> naInloggen = get(inloggen.resolve(link.group(1).replace("&amp;", "&")));
        assertThat(naInloggen.statusCode()).isEqualTo(302);
        assertThat(location(naInloggen)).endsWith(uitnodigingslink);
    }

    private String base() {
        return "http://localhost:" + port;
    }

    private URI volgRedirects(URI uri) throws IOException, InterruptedException {
        HttpResponse<String> response = get(uri);
        while (response.statusCode() == 302) {
            uri = uri.resolve(location(response));
            response = get(uri);
        }
        return uri;
    }

    private HttpResponse<String> get(URI uri) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(uri)
                .header("Sec-Fetch-Site", "same-origin")
                .header("Sec-Fetch-Mode", "navigate")
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static String location(HttpResponse<?> response) {
        return response.headers().firstValue("Location").orElseThrow();
    }
}
