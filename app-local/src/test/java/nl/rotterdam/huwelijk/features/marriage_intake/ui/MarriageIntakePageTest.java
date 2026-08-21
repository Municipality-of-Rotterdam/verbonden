package nl.rotterdam.huwelijk.features.marriage_intake.ui;

import nl.rotterdam.huwelijk.integration_test.BaseWicketTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;

import static org.assertj.core.api.Assertions.assertThat;

class MarriageIntakePageTest extends BaseWicketTest {

    @Autowired
    private TestRestTemplate restTemplate;

    /**
     * WicketTester bypasses the Spring Security HTTP filter chain, so this test renders the
     * page directly. The {@code @WithMockUser} annotation establishes an authenticated
     * Spring Security context, making the intent explicit: this page is for logged-in citizens.
     */
    @Test
    @WithMockUser
    void testRender() {
        tester.startPage(MarriageIntakePage.class);
        tester.assertRenderedPage(MarriageIntakePage.class);
    }

    /**
     * Verifies that an unauthenticated HTTP request to the home page is redirected to the
     * citizen login page ({@code /inloggen}). This test exercises the real Spring Security
     * filter chain, unlike the WicketTester-based test above.
     */
    @Test
    void unauthenticatedRequestRedirectsToLogin() {
        ResponseEntity<String> response = restTemplate.getForEntity("/", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FOUND);
        assertThat(response.getHeaders().getLocation()).hasPath("/inloggen");
    }
}
