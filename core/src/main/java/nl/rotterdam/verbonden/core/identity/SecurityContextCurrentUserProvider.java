package nl.rotterdam.verbonden.core.identity;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Standaardimplementatie van {@link CurrentUserProvider}, leest de actieve
 * {@code Authentication} uit {@code SecurityContextHolder}.
 */
@Component
class SecurityContextCurrentUserProvider implements CurrentUserProvider {

    private static final String ROLE_PREFIX = "ROLE_";
    private static final String GROUP_PREFIX = "vbr_";
    private static final String ONBEKEND = "onbekend";

    @Override
    public AuthenticatedUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth != null && auth.getName() != null ? auth.getName() : ONBEKEND;
        Set<String> roles = auth == null
                ? Set.of()
                : auth.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .map(SecurityContextCurrentUserProvider::normaliseerRol)
                        .collect(Collectors.toUnmodifiableSet());
        return new AuthenticatedUserImpl(userId, roles);
    }

    private static String normaliseerRol(String authority) {
        String role = authority.startsWith(ROLE_PREFIX)
                ? authority.substring(ROLE_PREFIX.length())
                : authority;
        if (role.regionMatches(true, 0, GROUP_PREFIX, 0, GROUP_PREFIX.length())) {
            return role.substring(GROUP_PREFIX.length()).toUpperCase(Locale.ROOT);
        }
        return role;
    }

    private record AuthenticatedUserImpl(String userId, Set<String> roles) implements AuthenticatedUser {
        @Override
        public String getUserId() {
            return userId;
        }

        @Override
        public Set<String> getRoles() {
            return roles;
        }
    }
}
