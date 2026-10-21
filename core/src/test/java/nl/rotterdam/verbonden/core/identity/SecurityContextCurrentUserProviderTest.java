package nl.rotterdam.verbonden.core.identity;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityContextCurrentUserProviderTest {

    private final SecurityContextCurrentUserProvider provider = new SecurityContextCurrentUserProvider();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void mapsMunicipalAdminGroupsToWicketRoles() {
        setAuthorities("vbr_babs", "ROLE_vbr_beheer");

        AuthenticatedUser currentUser = provider.getCurrentUser();

        assertThat(currentUser.getRoles()).containsExactlyInAnyOrder(Roles.BABS, Roles.BEHEER);
    }

    @Test
    void normalizesGroupPrefixWithoutCaseSensitivityAndPreservesUnknownGroups() {
        setAuthorities("VBR_BABS", "vbr_other");

        AuthenticatedUser currentUser = provider.getCurrentUser();

        assertThat(currentUser.getRoles()).containsExactlyInAnyOrder(Roles.BABS, "OTHER");
    }

    @Test
    void keepsStandardSpringRoleAuthorities() {
        setAuthorities("ROLE_BABS", "ROLE_BEHEER");

        AuthenticatedUser currentUser = provider.getCurrentUser();

        assertThat(currentUser.getRoles()).containsExactlyInAnyOrder(Roles.BABS, Roles.BEHEER);
    }

    private static void setAuthorities(String... authorities) {
        SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationToken.authenticated(
                "medewerker", null, List.of(authorities).stream().map(SimpleGrantedAuthority::new).toList()));
    }
}
